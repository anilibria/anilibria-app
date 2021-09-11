package ru.radiationx.anilibria.ui.common.webpage

sealed class WebPageViewState {
    object Success : WebPageViewState()
    object Loading : WebPageViewState()
    class Error(val error: WebPageError) : WebPageViewState()
}