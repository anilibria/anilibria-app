package ru.radiationx.data.apinext.models.filters

data class CatalogFilterData(
    val ageRatings: List<FilterItem.Value>?,
    val genres: List<FilterItem.Genre>?,
    val productionStatuses: List<FilterItem.Value>?,
    val publishStatuses: List<FilterItem.Value>?,
    val types: List<FilterItem.Value>?,
    val seasons: List<FilterItem.Value>?,
    val sortings: List<FilterItem.Value>?,
    val years: List<FilterItem.Year>?,
)

