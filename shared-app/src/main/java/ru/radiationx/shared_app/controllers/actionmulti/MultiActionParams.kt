package ru.radiationx.shared_app.controllers.actionmulti

data class MultiActionParams<KEY, ARG>(
    val key: KEY,
    val arg: ARG
)