package ru.radiationx.data.api.collections.models

import ru.radiationx.data.api.shared.filter.FormItem

data class CollectionsFilterForm(
    val query: String,
    val ageRatings: Set<FormItem.Value>,
    val genres: Set<FormItem.Genre>,
    val types: Set<FormItem.Value>,
    val years: Set<FormItem.Year>,
)