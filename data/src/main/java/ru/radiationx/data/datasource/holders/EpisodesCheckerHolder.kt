package ru.radiationx.data.datasource.holders

import kotlinx.coroutines.flow.Flow
import ru.radiationx.data.entity.app.release.ReleaseFull

interface EpisodesCheckerHolder {
    fun observeEpisodes(): Flow<List<ReleaseFull.Episode>>
    suspend fun getEpisodes(): List<ReleaseFull.Episode>
    fun putEpisode(episode: ReleaseFull.Episode)
    fun putAllEpisode(episodes: List<ReleaseFull.Episode>)
    fun getEpisodes(releaseId: Int): List<ReleaseFull.Episode>
    fun remove(releaseId: Int)
}