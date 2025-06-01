package ru.radiationx.data.api.collections.models

import ru.radiationx.data.api.shared.filter.FilterItem

data class CollectionsFilterData(
    val ageRatings: List<FilterItem.Value>?,
    val genres: List<FilterItem.Genre>?,
    val types: List<FilterItem.Value>?,
    val years: List<FilterItem.Year>?,
)

