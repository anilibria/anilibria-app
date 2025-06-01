package ru.radiationx.data.app.config.models

data class ConfigScreenState(
    val status: String = "",
    val needRefresh: Boolean = false,
    val hasNext: Boolean = false
)