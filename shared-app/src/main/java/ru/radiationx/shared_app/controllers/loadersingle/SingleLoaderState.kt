package ru.radiationx.shared_app.controllers.loadersingle

data class SingleLoaderState<DATA>(
    val loading: Boolean = false,
    val error: Throwable? = null,
    val data: DATA? = null
)