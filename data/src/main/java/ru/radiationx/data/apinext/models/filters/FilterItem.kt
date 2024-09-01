package ru.radiationx.data.apinext.models.filters

sealed interface FilterItem {
    data class Value(val value: String, val title: String, val description: String?) : FilterItem
    data class Genre(val id: Int, val title: String) : FilterItem
    data class Year(val year: Int, val title: String) : FilterItem
}