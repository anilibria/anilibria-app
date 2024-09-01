package ru.radiationx.data.apinext.models.filters

data class CatalogFilterForm(
    val query: String?,
    val ageRatings: List<FilterItem.Value>?,
    val genres: List<FilterItem.Genre>?,
    val productionStatuses: List<FilterItem.Value>?,
    val publishStatuses: List<FilterItem.Value>?,
    val types: List<FilterItem.Value>?,
    val seasons: List<FilterItem.Value>?,
    val sorting: FilterItem.Value?,
    val yearsRange: Pair<FilterItem.Year, FilterItem.Year>?
)