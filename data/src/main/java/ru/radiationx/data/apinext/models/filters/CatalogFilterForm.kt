package ru.radiationx.data.apinext.models.filters

data class CatalogFilterForm(
    val query: String,
    val ageRatings: Set<FormItem.Value>,
    val genres: Set<FormItem.Genre>,
    val productionStatuses: Set<FormItem.Value>,
    val publishStatuses: Set<FormItem.Value>,
    val types: Set<FormItem.Value>,
    val seasons: Set<FormItem.Value>,
    val sorting: FormItem.Value?,
    val yearsRange: Pair<FormItem.Year, FormItem.Year>?
)