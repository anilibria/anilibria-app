package ru.radiationx.shared_app.controllers.loaderpage

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.radiationx.shared.ktx.coRunCatching
import timber.log.Timber

class PageLoader<ARG, T>(
    private val coroutineScope: CoroutineScope,
    private val firstPage: Int = 1,
    private val dataSource: suspend PageLoaderParams<T>.(ARG) -> PageLoaderAction.Data<T>
) {

    private val _currentArg = MutableStateFlow<ARG?>(null)

    private val _currentPage = MutableStateFlow(firstPage)

    private var loadingJob: Job? = null

    private val _state = MutableStateFlow(PageLoaderState<T>())

    fun observePage(): StateFlow<Int> {
        return _currentPage
    }

    fun observeState(): StateFlow<PageLoaderState<T>> {
        return _state
    }

    fun reset() {
        release()
        _currentPage.value = firstPage
        _state.value = PageLoaderState()
    }

    fun refresh(arg: ARG) {
        loadPage(firstPage, arg)
    }

    fun loadMore() {
        if (_state.value.hasMorePages) {
            val arg = _currentArg.value ?: return
            loadPage(_currentPage.value + 1, arg)
        }
    }

    fun release() {
        loadingJob?.cancel()
    }

    fun getData(): T? {
        return _state.value.data
    }

    fun modifyData(data: T?) {
        val action = PageLoaderAction.DataModify(data)
        updateStateByAction(action, createPageLoadParams(_currentPage.value))
    }

    fun modifyData(block: (T) -> T) {
        val newData = _state.value.data?.let(block)
        modifyData(newData)
    }

    private fun loadPage(page: Int, arg: ARG) {
        if (loadingJob?.isActive == true) {
            return
        }

        val params = createPageLoadParams(page)

        val startLoadingAction: PageLoaderAction<T>? = when {
            params.isEmptyLoading -> PageLoaderAction.EmptyLoading()
            params.isRefreshLoading -> PageLoaderAction.Refresh()
            params.isMoreLoading -> PageLoaderAction.MoreLoading()
            else -> null
        }
        if (startLoadingAction != null) {
            updateStateByAction(startLoadingAction, params)
        }

        loadingJob = coroutineScope.launch {
            coRunCatching {
                dataSource.invoke(params, arg)
            }.onSuccess { dataAction ->
                _currentPage.value = page
                _currentArg.value = arg
                updateStateByAction(dataAction, params)
            }.onFailure { error ->
                Timber.e("page=$page", error)
                updateStateByAction(PageLoaderAction.Error(error), params)
            }
        }
    }

    private fun createPageLoadParams(page: Int): PageLoaderParams<T> {
        val isFirstPage = page == firstPage
        val isEmptyData = _state.value.data == null
        return PageLoaderParams(
            page = page,
            isFirstPage = isFirstPage,
            isDataEmpty = isEmptyData,
            isEmptyLoading = isFirstPage && isEmptyData,
            isRefreshLoading = isFirstPage && !isEmptyData,
            isMoreLoading = !isFirstPage,
            currentData = _state.value.data
        )
    }

    private fun updateStateByAction(action: PageLoaderAction<T>, params: PageLoaderParams<T>) {
        _state.update {
            it.applyAction(action, params)
        }
    }

    private fun <T> PageLoaderState<T>.applyAction(
        action: PageLoaderAction<T>,
        params: PageLoaderParams<T>
    ): PageLoaderState<T> = when (action) {
        is PageLoaderAction.EmptyLoading -> copy(
            emptyLoading = true,
            error = null
        )

        is PageLoaderAction.MoreLoading -> copy(
            moreLoading = true,
            error = null
        )

        is PageLoaderAction.Refresh -> copy(
            refreshLoading = true
        )

        is PageLoaderAction.Data -> copy(
            emptyLoading = false,
            refreshLoading = false,
            moreLoading = false,
            hasMorePages = action.hasMoreData ?: hasMorePages,
            data = action.data,
            error = null
        )

        is PageLoaderAction.DataModify -> copy(
            data = action.data,
            error = null
        )

        is PageLoaderAction.Error -> copy(
            emptyLoading = false,
            refreshLoading = false,
            moreLoading = false,
            data = data.takeIf { !params.isFirstPage },
            error = action.error
        )
    }.copy(
        initialState = false,
        isFirstPage = params.isFirstPage
    )
}