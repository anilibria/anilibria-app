package ru.radiationx.shared_app.controllers.actionsingle

data class SingleEventSuccess<ARG, RESULT>(
    val arg: ARG,
    val result: RESULT
)