package ru.radiationx.anilibria.model.loading

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ru.radiationx.shared.ktx.coRunCatching

class DataLoadingController<T>(
    private val scope: CoroutineScope,
    private val firstPage: Int = 1,
    private val dataSource: (suspend (PageLoadParams) -> ScreenStateAction.Data<T>),
) {

    private val stateRelay = MutableStateFlow(DataLoadingState<T>())

    private var currentPage = firstPage

    private var dataJob: Job? = null

    var currentState: DataLoadingState<T>
        get() = requireNotNull(stateRelay.value)
        private set(value) {
            stateRelay.value = value
        }

    fun observeState(): Flow<DataLoadingState<T>> {
        return stateRelay
    }

    fun refresh() {
        loadPage(firstPage)
    }


    fun loadMore() {
        if (currentState.hasMorePages) {
            loadPage(currentPage + 1)
        }
    }

    fun release() {
        dataJob?.cancel()
    }

    fun modifyData(data: T?) {
        val action = ScreenStateAction.DataModify(data)
        updateStateByAction(action)
    }

    private fun loadPage(page: Int) {
        if (dataJob?.isActive == true) {
            return
        }

        val params = createPageLoadParams(page)

        val startLoadingAction: ScreenStateAction<T>? = when {
            params.isEmptyLoading -> ScreenStateAction.EmptyLoading()
            params.isRefreshLoading -> ScreenStateAction.Refresh()
            params.isMoreLoading -> ScreenStateAction.MoreLoading()
            else -> null
        }
        if (startLoadingAction != null) {
            updateStateByAction(startLoadingAction)
        }

        dataJob?.cancel()
        dataJob = scope.launch {
            coRunCatching {
                dataSource.invoke(params)
            }.onSuccess {
                updateStateByAction(it)
                currentPage = page
            }.onFailure {
                updateStateByAction(ScreenStateAction.Error(it))
            }
        }
    }

    private fun createPageLoadParams(page: Int): PageLoadParams {
        val isFirstPage = page == firstPage
        val isEmptyData = stateRelay.value.data == null
        return PageLoadParams(
            page = page,
            isFirstPage = isFirstPage,
            isDataEmpty = isEmptyData,
            isEmptyLoading = isFirstPage && isEmptyData,
            isRefreshLoading = isFirstPage && !isEmptyData,
            isMoreLoading = !isFirstPage
        )
    }

    private fun updateState(block: (DataLoadingState<T>) -> DataLoadingState<T>) {
        currentState = block.invoke(currentState)
    }

    private fun updateStateByAction(action: ScreenStateAction<T>) {
        updateState {
            it.applyAction(action)
        }
    }
}

data class DataLoadingStateInfo(
    val initialState: Boolean = false,
    val emptyLoading: Boolean = false,
    val refreshLoading: Boolean = false,
    val moreLoading: Boolean = false,
    val hasMorePages: Boolean = false,
    val hasError: Boolean = false,
    val hasData: Boolean = false
)

fun DataLoadingState<*>.toInfo() = this.let {
    DataLoadingStateInfo(
        it.initialState,
        it.emptyLoading,
        it.refreshLoading,
        it.moreLoading,
        it.hasMorePages,
        it.error != null,
        it.data != null
    )
}