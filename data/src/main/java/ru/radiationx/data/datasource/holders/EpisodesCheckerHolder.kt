package ru.radiationx.data.datasource.holders

import kotlinx.coroutines.flow.Flow
import ru.radiationx.data.entity.domain.release.EpisodeAccess
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.data.entity.domain.types.ReleaseId

interface EpisodesCheckerHolder {
    fun observeEpisodes(): Flow<List<EpisodeAccess>>
    suspend fun getEpisodes(): List<EpisodeAccess>
    suspend fun putEpisode(episode: EpisodeAccess)
    suspend fun putAllEpisode(episodes: List<EpisodeAccess>)
    suspend fun getEpisodes(releaseId: ReleaseId): List<EpisodeAccess>
    suspend fun getEpisode(episodeId: EpisodeId): EpisodeAccess?
    suspend fun remove(releaseId: ReleaseId)
}