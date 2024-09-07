package ru.radiationx.shared_app.controllers.loaderpage

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.radiationx.shared.ktx.coRunCatching
import ru.radiationx.shared_app.controllers.actionsingle.SingleEventError
import ru.radiationx.shared_app.controllers.actionsingle.SingleEventSuccess
import timber.log.Timber

class PageLoader<T>(
    private val coroutineScope: CoroutineScope,
    private val firstPage: Int = 1,
    private val dataSource: suspend (PageLoaderParams<T>) -> PageLoaderAction.Data<T>
) {

    private var currentPage = firstPage

    private var loadingJob: Job? = null

    private val _state = MutableStateFlow(PageLoaderState<T>())
    val state = _state.asStateFlow()

    private val _actionSuccess =
        MutableSharedFlow<SingleEventSuccess<PageLoaderParams<T>, PageLoaderAction.Data<T>>>()
    val actionSuccess = _actionSuccess.asSharedFlow()

    private val _actionError = MutableSharedFlow<SingleEventError<PageLoaderParams<T>>>()
    val actionError = _actionError.asSharedFlow()

    fun reset() {
        release()
        currentPage = firstPage
        _state.value = PageLoaderState()
    }

    fun refresh() {
        loadPage(firstPage)
    }

    fun loadMore() {
        if (_state.value.hasMorePages) {
            loadPage(currentPage + 1)
        }
    }

    fun release() {
        loadingJob?.cancel()
    }

    fun modifyData(data: T?) {
        val action = PageLoaderAction.DataModify(data)
        updateStateByAction(action, createPageLoadParams(currentPage))
    }

    fun modifyData(block: (T) -> T) {
        val newData = _state.value.data?.let(block)
        modifyData(newData)
    }

    private fun loadPage(page: Int) {
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
                dataSource.invoke(params)
            }.onSuccess { dataAction ->
                currentPage = page
                updateStateByAction(dataAction, params)
                _actionSuccess.emit(SingleEventSuccess(params, dataAction))
            }.onFailure { error ->
                Timber.e("page=$page", error)
                updateStateByAction(PageLoaderAction.Error(error), params)
                _actionError.emit(SingleEventError(params, error))
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

}