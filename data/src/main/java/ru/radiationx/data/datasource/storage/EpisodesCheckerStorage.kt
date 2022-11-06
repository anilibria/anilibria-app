package ru.radiationx.data.datasource.storage

import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import ru.radiationx.data.DataPreferences
import ru.radiationx.data.datasource.holders.EpisodesCheckerHolder
import ru.radiationx.data.entity.db.EpisodeAccessDb
import ru.radiationx.data.entity.domain.release.EpisodeAccess
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.entity.mapper.toDb
import ru.radiationx.data.entity.mapper.toDomain
import javax.inject.Inject

/**
 * Created by radiationx on 17.02.18.
 */
class EpisodesCheckerStorage @Inject constructor(
    @DataPreferences private val sharedPreferences: SharedPreferences,
    private val moshi: Moshi
) : EpisodesCheckerHolder {

    companion object {
        private const val LOCAL_EPISODES_KEY = "data.local_episodes"
    }

    private val dataAdapter by lazy {
        val type = Types.newParameterizedType(List::class.java, EpisodeAccessDb::class.java)
        moshi.adapter<List<EpisodeAccessDb>>(type)
    }

    private val localEpisodesRelay by lazy {
        MutableStateFlow(loadAll())
    }

    override fun observeEpisodes(): Flow<List<EpisodeAccess>> =
        localEpisodesRelay

    override suspend fun getEpisodes(): List<EpisodeAccess> {
        return localEpisodesRelay.value
    }

    override fun putEpisode(episode: EpisodeAccess) {
        localEpisodesRelay.update { localEpisodes ->
            val mutableLocalEpisodes = localEpisodes.toMutableList()
            mutableLocalEpisodes
                .firstOrNull { it.id == episode.id }
                ?.let { mutableLocalEpisodes.remove(it) }
            mutableLocalEpisodes.add(episode)
            mutableLocalEpisodes
        }
        saveAll()
    }

    override fun putAllEpisode(episodes: List<EpisodeAccess>) {
        localEpisodesRelay.update { localEpisodes ->
            val mutableLocalEpisodes = localEpisodes.toMutableList()
            episodes.forEach { episode ->
                mutableLocalEpisodes
                    .firstOrNull { it.id == episode.id }
                    ?.let { mutableLocalEpisodes.remove(it) }
                mutableLocalEpisodes.add(episode)
            }
            mutableLocalEpisodes
        }
        saveAll()
    }

    override fun getEpisodes(releaseId: ReleaseId): List<EpisodeAccess> {
        return localEpisodesRelay.value.filter { it.id.releaseId == releaseId }
    }

    override fun remove(releaseId: ReleaseId) {
        localEpisodesRelay.update { localEpisodes ->
            val mutableLocalEpisodes = localEpisodes.toMutableList()
            mutableLocalEpisodes.removeAll { it.id.releaseId == releaseId }
            mutableLocalEpisodes
        }
        saveAll()
    }

    private fun saveAll() {
        val jsonEpisodes = localEpisodesRelay.value
            .map { it.toDb() }
            .let { dataAdapter.toJson(it) }
        sharedPreferences
            .edit()
            .putString(LOCAL_EPISODES_KEY, jsonEpisodes)
            .apply()
    }

    private fun loadAll(): List<EpisodeAccess> = sharedPreferences
        .getString(LOCAL_EPISODES_KEY, null)
        ?.let { dataAdapter.fromJson(it) }
        ?.map { it.toDomain() }
        .orEmpty()
}