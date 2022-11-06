package ru.radiationx.data.datasource.holders

import kotlinx.coroutines.flow.Flow
import ru.radiationx.data.entity.domain.release.EpisodeAccess
import ru.radiationx.data.entity.domain.types.ReleaseId

interface EpisodesCheckerHolder {
    fun observeEpisodes(): Flow<List<EpisodeAccess>>
    suspend fun getEpisodes(): List<EpisodeAccess>
    fun putEpisode(episode: EpisodeAccess)
    fun putAllEpisode(episodes: List<EpisodeAccess>)
    fun getEpisodes(releaseId: ReleaseId): List<EpisodeAccess>
    fun remove(releaseId: ReleaseId)
}