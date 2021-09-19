package ru.radiationx.anilibria.model.loading

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables

class DataLoadingController<T>(
    private val firstPage: Int = 1,
    private val dataSource: (PageLoadParams) -> Single<ScreenStateAction.Data<T>>
) : Disposable {

    private val stateRelay = BehaviorRelay.createDefault(DataLoadingState<T>())

    private var currentPage = firstPage

    private var dataDisposable = Disposables.disposed()

    var currentState: DataLoadingState<T>
        get() = requireNotNull(stateRelay.value)
        private set(value) {
            stateRelay.accept(value)
        }

    fun observeState(): Observable<DataLoadingState<T>> {
        return stateRelay.hide().distinctUntilChanged()
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
        dataDisposable.dispose()
    }

    fun modifyData(data: T?) {
        val action = ScreenStateAction.DataModify(data)
        updateStateByAction(action)
    }

    private fun loadPage(page: Int) {
        if (!dataDisposable.isDisposed) {
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

        dataDisposable = dataSource
            .invoke(params)
            .subscribe({ dataAction ->
                updateStateByAction(dataAction)
                currentPage = page
            }) { throwable ->
                updateStateByAction(ScreenStateAction.Error(throwable))
            }
    }

    private fun createPageLoadParams(page: Int): PageLoadParams {
        val isFirstPage = page == firstPage
        val isEmptyData = stateRelay.value?.data == null
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

    override fun dispose() {
        dataDisposable.dispose()
    }

    override fun isDisposed(): Boolean {
        return dataDisposable.isDisposed
    }
}

data class DataLoadingStateInfo(
    val emptyLoading: Boolean = false,
    val refreshLoading: Boolean = false,
    val moreLoading: Boolean = false,
    val hasMorePages: Boolean = false,
    val hasError: Boolean = false,
    val hasData: Boolean = false
)

fun DataLoadingState<*>.toInfo() = this.let {
    DataLoadingStateInfo(
        it.emptyLoading,
        it.refreshLoading,
        it.moreLoading,
        it.hasMorePages,
        it.error != null,
        it.data != null
    )
}