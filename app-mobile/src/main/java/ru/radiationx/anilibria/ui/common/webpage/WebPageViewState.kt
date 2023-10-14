package ru.radiationx.anilibria.ui.common.webpage

sealed class WebPageViewState {
    data object Success : WebPageViewState()
    data object Loading : WebPageViewState()
    data class Error(val error: WebPageError) : WebPageViewState()
}