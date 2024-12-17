package ru.radiationx.shared_app.controllers.loaderpage

data class PageLoaderState<T>(
    val initialState: Boolean,
    val emptyLoading: Boolean,
    val refreshLoading: Boolean,
    val moreLoading: Boolean,
    val hasMoreData: Boolean,
    val isFirstPage: Boolean,
    val error: Throwable?,
    val data: T?
) {
    companion object {
        fun <T> empty(): PageLoaderState<T> = PageLoaderState(
            initialState = true,
            emptyLoading = false,
            refreshLoading = false,
            moreLoading = false,
            hasMoreData = false,
            isFirstPage = true,
            error = null,
            data = null
        )
    }
}

