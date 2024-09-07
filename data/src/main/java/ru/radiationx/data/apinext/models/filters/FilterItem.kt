package ru.radiationx.data.apinext.models.filters

import ru.radiationx.data.entity.domain.types.GenreId

sealed interface FilterItem {
    data class Value(val value: String, val title: String, val description: String?) : FilterItem
    data class Genre(val id: GenreId, val title: String) : FilterItem
    data class Year(val year: Int, val title: String) : FilterItem
}

sealed interface FormItem {
    data class Value(val value: String) : FormItem
    data class Genre(val id: GenreId) : FormItem
    data class Year(val year: Int) : FormItem
}