package ru.radiationx.anilibria.model.loading

data class PageLoadParams(
    val page: Int,
    val isFirstPage: Boolean,
    val isDataEmpty: Boolean,
    val isEmptyLoading: Boolean,
    val isRefreshLoading: Boolean,
    val isMoreLoading: Boolean
)
