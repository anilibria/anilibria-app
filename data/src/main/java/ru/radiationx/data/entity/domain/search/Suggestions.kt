package ru.radiationx.data.entity.domain.search

import ru.radiationx.data.entity.domain.release.Release

data class Suggestions(
    val query: String,
    val items: List<Release>
)
