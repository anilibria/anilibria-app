package ru.radiationx.shared_app.controllers.loaderpage

data class PageLoaderState<T>(
    val initialState: Boolean = true,
    val emptyLoading: Boolean = false,
    val refreshLoading: Boolean = false,
    val moreLoading: Boolean = false,
    val hasMoreData: Boolean = false,
    val isFirstPage: Boolean = true,
    val error: Throwable? = null,
    val data: T? = null
)

