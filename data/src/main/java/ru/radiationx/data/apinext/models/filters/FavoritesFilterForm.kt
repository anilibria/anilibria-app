package ru.radiationx.data.apinext.models.filters

data class FavoritesFilterForm(
    val query: String?,
    val ageRatings: List<FilterItem.Value>?,
    val genres: List<FilterItem.Genre>?,
    val types: List<FilterItem.Value>?,
    val sorting: FilterItem.Value?,
    val years: List<FilterItem.Year>?,
)