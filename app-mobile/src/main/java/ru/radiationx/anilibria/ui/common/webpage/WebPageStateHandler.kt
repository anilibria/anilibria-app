package ru.radiationx.anilibria.ui.common.webpage

class WebPageStateHandler(
    private val listener: (WebPageViewState) -> Unit
) {

    private var currentLoading = false
    private var currentError: WebPageError? = null
    private var currentState: WebPageViewState? = null

    fun onLoadingChanged(isLoading: Boolean) {
        if (isLoading) {
            currentError = null
        }
        currentLoading = isLoading
        updateState()
    }

    fun onError(error: WebPageError) {
        currentLoading = false
        currentError = error
        updateState()
    }

    private fun updateState() {
        val error = currentError
        val state = when {
            currentLoading -> WebPageViewState.Loading
            error != null -> WebPageViewState.Error(error)
            else -> WebPageViewState.Success
        }
        if (currentState != state) {
            currentState = state
            listener.invoke(state)
        }
    }
}