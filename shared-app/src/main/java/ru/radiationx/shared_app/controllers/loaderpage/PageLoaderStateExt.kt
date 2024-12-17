package ru.radiationx.shared_app.controllers.loaderpage


fun PageLoaderState<*>.hasAnyLoading(): Boolean {
    return emptyLoading || refreshLoading || moreLoading
}

fun <T> PageLoaderState<T>.needShowData(condition: (T?) -> Boolean = { it != null }): Boolean {
    return condition.invoke(data)
}

fun <T> PageLoaderState<List<T>>.needShowListData() = needShowData { it?.isNotEmpty() ?: false }


fun <T> PageLoaderState<T>.needShowEmpty(dataCondition: (T?) -> Boolean = { it != null }): Boolean {
    return isFirstPage && !needShowData(dataCondition) && !emptyLoading && !initialState && error == null
}

fun <T> PageLoaderState<List<T>>.needShowListEmpty(): Boolean {
    return isFirstPage && !needShowListData() && !emptyLoading && !initialState && error == null
}

fun <T> PageLoaderState<T>.needShowError(): Boolean {
    return isFirstPage && error != null && !needShowData() && !emptyLoading && !initialState
}

fun <T> PageLoaderState<List<T>>.needShowListError(): Boolean {
    return isFirstPage && error != null && !needShowListData() && !emptyLoading && !initialState
}

fun <T> PageLoaderState<T>.needShowPlaceholder(dataCondition: (T?) -> Boolean = { it != null }): Boolean {
    return !needShowData(dataCondition) && !emptyLoading && !initialState
}

fun <T, R> PageLoaderState<T>.mapData(
    newDataMapper: (T) -> R
): PageLoaderState<R> = PageLoaderState(
    initialState = initialState,
    emptyLoading = emptyLoading,
    refreshLoading = refreshLoading,
    moreLoading = moreLoading,
    hasMoreData = hasMoreData,
    error = error,
    data = data?.let(newDataMapper)
)

fun <T> PageLoaderState<T>.applyAction(
    action: PageLoaderAction<T>,
    params: PageLoaderParams<T>
): PageLoaderState<T> {
    return when (action) {
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
            hasMoreData = action.hasMoreData ?: hasMoreData,
            data = action.data,
            error = null
        )

        is PageLoaderAction.ModifyData -> copy(
            hasMoreData = action.hasMoreData ?: hasMoreData,
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