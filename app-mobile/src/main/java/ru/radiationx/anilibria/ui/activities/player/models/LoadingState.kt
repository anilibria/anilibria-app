package ru.radiationx.anilibria.ui.activities.player.models

data class LoadingState<T>(
    val loading: Boolean = false,
    val data: T? = null,
    val error: Throwable? = null,
)