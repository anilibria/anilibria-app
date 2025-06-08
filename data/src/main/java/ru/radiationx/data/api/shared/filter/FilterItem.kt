package ru.radiationx.data.api.shared.filter

import ru.radiationx.data.common.GenreId

sealed interface FilterItem<T : FormItem> {

    val item: T
    val title: String

    data class Value(
        override val item: FormItem.Value,
        override val title: String,
        val description: String?
    ) : FilterItem<FormItem.Value>

    data class Genre(
        override val item: FormItem.Genre,
        override val title: String
    ) : FilterItem<FormItem.Genre>

    data class Year(
        override val item: FormItem.Year,
        override val title: String
    ) : FilterItem<FormItem.Year>
}

sealed interface FormItem {

    data class Value(val value: String) : FormItem

    data class Genre(val id: GenreId) : FormItem

    data class Year(val year: Int) : FormItem
}