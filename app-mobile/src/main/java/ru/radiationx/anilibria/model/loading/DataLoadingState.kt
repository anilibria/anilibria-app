package ru.radiationx.anilibria.model.loading

data class DataLoadingState<T>(
    val emptyLoading: Boolean = false,
    val refreshLoading: Boolean = false,
    val moreLoading: Boolean = false,
    val hasMorePages: Boolean = false,
    val error: Throwable? = null,
    val data: T? = null
)

fun DataLoadingState<*>.hasAnyLoading(): Boolean {
    return emptyLoading || refreshLoading || moreLoading
}

fun <T> DataLoadingState<T>.applyAction(action: ScreenStateAction<T>): DataLoadingState<T> {
    return when (action) {
        is ScreenStateAction.EmptyLoading -> copy(
            emptyLoading = true,
            error = null
        )
        is ScreenStateAction.MoreLoading -> copy(
            moreLoading = true,
            error = null
        )
        is ScreenStateAction.Refresh -> copy(
            refreshLoading = true
        )
        is ScreenStateAction.Data -> copy(
            emptyLoading = false,
            refreshLoading = false,
            moreLoading = false,
            hasMorePages = action.hasMoreData ?: hasMorePages,
            data = action.data,
            error = null
        )
        is ScreenStateAction.DataModify -> copy(
            data = action.data,
            error = null
        )
        is ScreenStateAction.Error -> copy(
            emptyLoading = false,
            refreshLoading = false,
            moreLoading = false,
            error = action.error
        )
    }
}

sealed class ScreenStateAction<T> {
    class EmptyLoading<T> : ScreenStateAction<T>()
    class MoreLoading<T> : ScreenStateAction<T>()
    class Refresh<T> : ScreenStateAction<T>()
    class Data<T>(val data: T?, val hasMoreData: Boolean? = null) : ScreenStateAction<T>()
    class DataModify<T>(val data: T?) : ScreenStateAction<T>()
    class Error<T>(val error: Throwable) : ScreenStateAction<T>()
}