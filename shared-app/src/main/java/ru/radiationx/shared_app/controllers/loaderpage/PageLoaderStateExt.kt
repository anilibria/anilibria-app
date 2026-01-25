package ru.radiationx.shared_app.controllers.loaderpage

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


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
    block: (T) -> R
): PageLoaderState<R> = PageLoaderState(
    initialState = initialState,
    emptyLoading = emptyLoading,
    refreshLoading = refreshLoading,
    moreLoading = moreLoading,
    hasMoreData = hasMoreData,
    isFirstPage = isFirstPage,
    error = error,
    data = data?.let(block)
)

fun <T, R> Flow<PageLoaderState<T>>.mapData(
    block: (T) -> R
): Flow<PageLoaderState<R>> = map {
    it.mapData(block)
}