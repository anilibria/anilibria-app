package ru.radiationx.data.entity.domain

import ru.radiationx.data.entity.domain.release.Release

data class HistoryReleases(
    val items: List<Release>,
    val total: Int
) {
    val hasMore = items.size < total
}