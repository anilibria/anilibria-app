package ru.radiationx.shared_app.controllers.actionmulti

data class MultiEventError<KEY, ARG>(
    val key: KEY,
    val arg: ARG,
    val error: Throwable
)