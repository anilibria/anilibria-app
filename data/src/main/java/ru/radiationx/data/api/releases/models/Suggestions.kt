package ru.radiationx.data.api.releases.models

data class Suggestions(
    val query: String,
    val items: List<Release>
)
