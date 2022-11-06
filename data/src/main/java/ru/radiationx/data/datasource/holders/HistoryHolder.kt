package ru.radiationx.data.datasource.holders

import kotlinx.coroutines.flow.Flow
import ru.radiationx.data.entity.domain.release.Release

interface HistoryHolder {
    suspend fun getEpisodes(): List<Release>
    fun observeEpisodes(): Flow<List<Release>>
    fun putRelease(release: Release)
    fun putAllRelease(releases: List<Release>)
    fun removerRelease(id: Int)
}