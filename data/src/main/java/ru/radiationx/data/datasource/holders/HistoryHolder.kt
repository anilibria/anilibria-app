package ru.radiationx.data.datasource.holders

import kotlinx.coroutines.flow.Flow
import ru.radiationx.data.entity.app.release.ReleaseItem

interface HistoryHolder {
    suspend fun getEpisodes(): List<ReleaseItem>
    fun observeEpisodes(): Flow<List<ReleaseItem>>
    fun putRelease(release: ReleaseItem)
    fun putAllRelease(releases: List<ReleaseItem>)
    fun removerRelease(id: Int)
}