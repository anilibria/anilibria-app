package ru.radiationx.shared_app.controllers.loaderpage

data class PageLoaderParams<T>(
    val page: Int,
    val isFirstPage: Boolean,
    val isDataEmpty: Boolean,
    val isEmptyLoading: Boolean,
    val isRefreshLoading: Boolean,
    val isMoreLoading: Boolean,
    val currentData: T?
)
