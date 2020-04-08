package ru.radiationx.data.entity.common

data class ConfigScreenState(
    var status: String = "",
    var needRefresh: Boolean = false,
    var hasNext: Boolean = false
)