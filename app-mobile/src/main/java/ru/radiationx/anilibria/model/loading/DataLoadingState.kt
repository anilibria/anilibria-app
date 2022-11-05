package ru.radiationx.anilibria.model.loading

data class DataLoadingState<T>(
    val initialState: Boolean = true,
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

fun <T> DataLoadingState<T>.hasData(condition: (T?) -> Boolean = { it != null }): Boolean {
    return condition.invoke(data)
}

fun <T> DataLoadingState<List<T>>.hasListData() = hasData { it?.isNotEmpty() ?: false }

fun <T> DataLoadingState<T>.needShowPlaceholder(dataCondition: (T?) -> Boolean = { it != null }): Boolean {
    return !hasData(dataCondition) && !emptyLoading && !initialState
}


fun <T> DataLoadingState<T>.applyAction(action: ScreenStateAction<T>): DataLoadingState<T> {
    return when (action) {
        is ScreenStateAction.EmptyLoading -> copy(
            initialState = false,
            emptyLoading = true,
            error = null
        )
        is ScreenStateAction.MoreLoading -> copy(
            initialState = false,
            moreLoading = true,
            error = null
        )
        is ScreenStateAction.Refresh -> copy(
            initialState = false,
            refreshLoading = true
        )
        is ScreenStateAction.Data -> copy(
            initialState = false,
            emptyLoading = false,
            refreshLoading = false,
            moreLoading = false,
            hasMorePages = action.hasMoreData ?: hasMorePages,
            data = action.data,
            error = null
        )
        is ScreenStateAction.DataModify -> copy(
            initialState = false,
            data = action.data,
            error = null
        )
        is ScreenStateAction.Error -> copy(
            initialState = false,
            emptyLoading = false,
            refreshLoading = false,
            moreLoading = false,
            error = action.error
        )
    }
}

fun <T, R> DataLoadingState<T>.mapData(
    newDataMapper: (T) -> R
): DataLoadingState<R> = DataLoadingState(
    initialState = initialState,
    emptyLoading = emptyLoading,
    refreshLoading = refreshLoading,
    moreLoading = moreLoading,
    hasMorePages = hasMorePages,
    error = error,
    data = data?.let(newDataMapper)
)

sealed class ScreenStateAction<T> {
    class EmptyLoading<T> : ScreenStateAction<T>()
    class MoreLoading<T> : ScreenStateAction<T>()
    class Refresh<T> : ScreenStateAction<T>()
    class Data<T>(val data: T?, val hasMoreData: Boolean? = null) : ScreenStateAction<T>()
    class DataModify<T>(val data: T?) : ScreenStateAction<T>()
    class Error<T>(val error: Throwable) : ScreenStateAction<T>()
}