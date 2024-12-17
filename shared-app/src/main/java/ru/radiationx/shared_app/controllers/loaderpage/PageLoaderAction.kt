package ru.radiationx.shared_app.controllers.loaderpage

sealed class PageLoaderAction<T> {
    class EmptyLoading<T> : PageLoaderAction<T>()
    class MoreLoading<T> : PageLoaderAction<T>()
    class Refresh<T> : PageLoaderAction<T>()
    class Data<T>(val data: T?, val hasMoreData: Boolean? = null) : PageLoaderAction<T>()
    class ModifyData<T>(val data: T?, val hasMoreData: Boolean? = null) : PageLoaderAction<T>()
    class Error<T>(val error: Throwable) : PageLoaderAction<T>()
}