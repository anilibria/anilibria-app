package ru.radiationx.data.app.episodeaccess

import kotlinx.coroutines.flow.Flow
import ru.radiationx.data.app.episodeaccess.models.EpisodeAccess
import ru.radiationx.data.common.EpisodeId
import ru.radiationx.data.common.ReleaseId

interface EpisodesCheckerHolder {
    fun observeEpisodes(): Flow<List<EpisodeAccess>>
    suspend fun getEpisodes(): List<EpisodeAccess>
    suspend fun putEpisode(episode: EpisodeAccess)
    suspend fun putAllEpisode(episodes: List<EpisodeAccess>)
    suspend fun getEpisodes(releaseId: ReleaseId): List<EpisodeAccess>
    suspend fun getEpisode(episodeId: EpisodeId): EpisodeAccess?
    suspend fun remove(releaseId: ReleaseId)
}