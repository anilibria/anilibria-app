package ru.radiationx.data.datasource.holders

import kotlinx.coroutines.flow.Flow
import ru.radiationx.data.entity.app.release.Episode

interface EpisodesCheckerHolder {
    fun observeEpisodes(): Flow<List<Episode>>
    suspend fun getEpisodes(): List<Episode>
    fun putEpisode(episode: Episode)
    fun putAllEpisode(episodes: List<Episode>)
    fun getEpisodes(releaseId: Int): List<Episode>
    fun remove(releaseId: Int)
}