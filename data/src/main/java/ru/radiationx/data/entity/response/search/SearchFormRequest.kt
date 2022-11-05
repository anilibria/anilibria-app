package ru.radiationx.data.entity.response.search

data class SearchFormRequest(
    val years: List<String>?,
    val seasons: List<String>?,
    val genres: List<String>?,
    val sort: String,
    val onlyCompleted: Boolean
)