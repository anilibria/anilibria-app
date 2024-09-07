package ru.radiationx.data.apinext.models.filters

data class FavoritesFilterForm(
    val query: String,
    val ageRatings: Set<FormItem.Value>,
    val genres: Set<FormItem.Genre>,
    val types: Set<FormItem.Value>,
    val sorting: FormItem.Value?,
    val years: Set<FormItem.Year>,
)