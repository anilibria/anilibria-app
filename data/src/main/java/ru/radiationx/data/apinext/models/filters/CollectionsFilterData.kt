package ru.radiationx.data.apinext.models.filters

data class CollectionsFilterData(
    val ageRatings: List<FilterItem.Value>?,
    val genres: List<FilterItem.Genre>?,
    val types: List<FilterItem.Value>?,
    val years: List<FilterItem.Year>?,
)

