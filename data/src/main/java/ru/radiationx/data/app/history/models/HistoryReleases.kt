package ru.radiationx.data.app.history.models

import ru.radiationx.data.api.releases.models.Release

data class HistoryReleases(
    val items: List<Release>,
    val total: Int
) {
    val hasMore = items.size < total
}