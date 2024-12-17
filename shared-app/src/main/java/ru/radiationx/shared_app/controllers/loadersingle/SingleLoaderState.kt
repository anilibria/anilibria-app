package ru.radiationx.shared_app.controllers.loadersingle

data class SingleLoaderState<DATA>(
    val loading: Boolean,
    val error: Throwable?,
    val data: DATA?
) {
    companion object {
        fun <DATA> empty(): SingleLoaderState<DATA> = SingleLoaderState(
            loading = false,
            error = null,
            data = null
        )
    }
}