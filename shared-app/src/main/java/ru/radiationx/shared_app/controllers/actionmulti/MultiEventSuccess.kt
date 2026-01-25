package ru.radiationx.shared_app.controllers.actionmulti

data class MultiEventSuccess<KEY, ARG, RESULT>(
    val key: KEY,
    val arg: ARG,
    val result: RESULT,
)