package ru.radiationx.data.entity.response.search

data class SuggestionResponse(
    val id: Int,
    val code: String,
    val names: List<String>,
    val poster: String?
)