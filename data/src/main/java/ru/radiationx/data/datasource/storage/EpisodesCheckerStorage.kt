package ru.radiationx.data.datasource.storage

import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.data.DataPreferences
import ru.radiationx.data.datasource.holders.EpisodesCheckerHolder
import ru.radiationx.data.entity.domain.release.EpisodeAccess
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.data.entity.domain.types.ReleaseId
import javax.inject.Inject

/**
 * Created by radiationx on 17.02.18.
 */
class EpisodesCheckerStorage @Inject constructor(
    @DataPreferences private val sharedPreferences: SharedPreferences
) : EpisodesCheckerHolder {

    companion object {
        private const val LOCAL_EPISODES_KEY = "data.local_episodes"
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
        val jsonEpisodes = JSONArray()
        localEpisodesRelay.value.forEach {
            jsonEpisodes.put(JSONObject().apply {
                put("releaseId", it.id.releaseId.id)
                put("id", it.id.id)
                put("seek", it.seek)
                put("isViewed", it.isViewed)
                put("lastAccess", it.lastAccess)
            })
        }
        sharedPreferences
            .edit()
            .putString(LOCAL_EPISODES_KEY, jsonEpisodes.toString())
            .apply()
    }

    private fun loadAll(): List<EpisodeAccess> {
        val result = mutableListOf<EpisodeAccess>()
        val savedEpisodes = sharedPreferences.getString(LOCAL_EPISODES_KEY, null)
        savedEpisodes?.let {
            val jsonEpisodes = JSONArray(it)
            (0 until jsonEpisodes.length()).forEach {
                jsonEpisodes.getJSONObject(it).let {
                    result.add(
                        EpisodeAccess(
                            id = EpisodeId(it.getInt("id"), ReleaseId(it.getInt("releaseId"))),
                            seek = it.optLong("seek", 0L),
                            isViewed = it.optBoolean("isViewed", false),
                            lastAccess = it.optLong("lastAccess", 0L),
                        )
                    )
                }
            }
        }
        return result
    }
}