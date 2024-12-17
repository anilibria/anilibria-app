package ru.radiationx.data.entity.domain.search

data class Suggestions(
    val query: String,
    val items: List<SuggestionItem>
)
