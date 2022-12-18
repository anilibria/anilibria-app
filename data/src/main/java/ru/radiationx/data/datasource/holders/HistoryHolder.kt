package ru.radiationx.data.datasource.holders

import kotlinx.coroutines.flow.Flow
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.types.ReleaseId

interface HistoryHolder {
    suspend fun getEpisodes(): List<Release>
    fun observeEpisodes(): Flow<List<Release>>
    suspend fun putRelease(release: Release)
    suspend fun putAllRelease(releases: List<Release>)
    suspend fun removerRelease(id: ReleaseId)
}