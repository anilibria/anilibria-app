package ru.radiationx.data.entity.domain.search

data class SuggestionItem(
    val id: Int,
    val code: String,
    val names: List<String>,
    val poster: String?
)