package ru.radiationx.data.entity.common

data class ConfigScreenState(
    val status: String = "",
    val needRefresh: Boolean = false,
    val hasNext: Boolean = false
)