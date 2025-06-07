package ru.radiationx.data.api.shared.filter

import ru.radiationx.data.common.GenreId

sealed interface FilterItem {

    data class Value(
        val item: FormItem.Value,
        val title: String,
        val description: String?
    ) : FilterItem

    data class Genre(
        val item: FormItem.Genre,
        val title: String
    ) : FilterItem

    data class Year(
        val item: FormItem.Year,
        val title: String
    ) : FilterItem
}

sealed interface FormItem {

    data class Value(val value: String) : FormItem

    data class Genre(val id: GenreId) : FormItem

    data class Year(val year: Int) : FormItem
}