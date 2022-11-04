package ru.radiationx.data.datasource.storage

import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.data.DataPreferences
import ru.radiationx.data.datasource.holders.EpisodesCheckerHolder
import ru.radiationx.data.entity.app.release.Episode
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

    override fun observeEpisodes(): Flow<List<Episode>> =
        localEpisodesRelay

    override suspend fun getEpisodes(): List<Episode> {
        return localEpisodesRelay.value
    }

    override fun putEpisode(episode: Episode) {
        localEpisodesRelay.update { localEpisodes ->
            val mutableLocalEpisodes = localEpisodes.toMutableList()
            mutableLocalEpisodes
                .firstOrNull { it.releaseId == episode.releaseId && it.id == episode.id }
                ?.let { mutableLocalEpisodes.remove(it) }
            mutableLocalEpisodes.add(episode)
            mutableLocalEpisodes
        }
        saveAll()
    }

    override fun putAllEpisode(episodes: List<Episode>) {
        localEpisodesRelay.update { localEpisodes ->
            val mutableLocalEpisodes = localEpisodes.toMutableList()
            episodes.forEach { episode ->
                mutableLocalEpisodes
                    .firstOrNull { it.releaseId == episode.releaseId && it.id == episode.id }
                    ?.let { mutableLocalEpisodes.remove(it) }
                mutableLocalEpisodes.add(episode)
            }
            mutableLocalEpisodes
        }
        saveAll()
    }

    override fun getEpisodes(releaseId: Int): List<Episode> {
        return localEpisodesRelay.value.filter { it.releaseId == releaseId }
    }

    override fun remove(releaseId: Int) {
        localEpisodesRelay.update { localEpisodes ->
            val mutableLocalEpisodes = localEpisodes.toMutableList()
            mutableLocalEpisodes.removeAll { it.releaseId == releaseId }
            mutableLocalEpisodes
        }
        saveAll()
    }

    private fun saveAll() {
        val jsonEpisodes = JSONArray()
        localEpisodesRelay.value.forEach {
            jsonEpisodes.put(JSONObject().apply {
                put("releaseId", it.releaseId)
                put("id", it.id)
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

    private fun loadAll(): List<Episode> {
        val result = mutableListOf<Episode>()
        val savedEpisodes = sharedPreferences.getString(LOCAL_EPISODES_KEY, null)
        savedEpisodes?.let {
            val jsonEpisodes = JSONArray(it)
            (0 until jsonEpisodes.length()).forEach {
                jsonEpisodes.getJSONObject(it).let {
                    result.add(Episode().apply {
                        releaseId = it.getInt("releaseId")
                        id = it.getInt("id")
                        seek = it.optLong("seek", 0L)
                        isViewed = it.optBoolean("isViewed", false)
                        lastAccess = it.optLong("lastAccess", 0L)
                    })
                }
            }
        }
        return result
    }
}