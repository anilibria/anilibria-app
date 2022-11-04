package ru.radiationx.data.datasource.holders

import kotlinx.coroutines.flow.Flow
import ru.radiationx.data.entity.app.release.EpisodeAccess

interface EpisodesCheckerHolder {
    fun observeEpisodes(): Flow<List<EpisodeAccess>>
    suspend fun getEpisodes(): List<EpisodeAccess>
    fun putEpisode(episode: EpisodeAccess)
    fun putAllEpisode(episodes: List<EpisodeAccess>)
    fun getEpisodes(releaseId: Int): List<EpisodeAccess>
    fun remove(releaseId: Int)
}