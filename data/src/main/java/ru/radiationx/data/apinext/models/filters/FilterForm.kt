package ru.radiationx.data.apinext.models.filters

data class FilterForm(
    val availableTypes: Set<FilterType>,
    val query: String?,
    val ageRatings: List<FilterItem.Value>?,
    val genres: List<FilterItem.Genre>?,
    val productionStatus: FilterItem.Value?,
    val publishStatus: FilterItem.Value?,
    val releaseTypes: List<FilterItem.Value>?,
    val season: List<FilterItem.Value>?,
    val sorting: FilterItem.Value?,
    val years: List<FilterItem.Year>?,
    val yearsRange: Pair<FilterItem.Year, FilterItem.Year>?
)