package ru.radiationx.shared_app.controllers.actionsingle

data class SingleEventError<ARG>(
    val arg: ARG,
    val error: Throwable
)