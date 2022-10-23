package ru.radiationx.data.datasource.storage

import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.data.DataPreferences
import ru.radiationx.data.datasource.holders.EpisodesCheckerHolder
import ru.radiationx.data.entity.app.release.ReleaseFull
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

    private val localEpisodes by lazy {
        loadAll().toMutableList()
    }
    private val localEpisodesRelay by lazy {
        MutableStateFlow(localEpisodes.toList())
    }

    override fun observeEpisodes(): Flow<List<ReleaseFull.Episode>> =
        localEpisodesRelay

    override suspend fun getEpisodes(): List<ReleaseFull.Episode> {
        return localEpisodesRelay.value
    }

    override fun putEpisode(episode: ReleaseFull.Episode) {
        localEpisodes
            .firstOrNull { it.releaseId == episode.releaseId && it.id == episode.id }
            ?.let { localEpisodes.remove(it) }
        localEpisodes.add(episode)
        saveAll()
        localEpisodesRelay.value = localEpisodes.toList()
    }

    override fun putAllEpisode(episodes: List<ReleaseFull.Episode>) {
        episodes.forEach { episode ->
            localEpisodes
                .firstOrNull { it.releaseId == episode.releaseId && it.id == episode.id }
                ?.let { localEpisodes.remove(it) }
            localEpisodes.add(episode)
        }
        saveAll()
        localEpisodesRelay.value = localEpisodes.toList()
    }

    override fun getEpisodes(releaseId: Int): List<ReleaseFull.Episode> {
        return localEpisodes.filter { it.releaseId == releaseId }
    }

    override fun remove(releaseId: Int) {
        localEpisodes.removeAll { it.releaseId == releaseId }
        saveAll()
        localEpisodesRelay.value = localEpisodes.toList()
    }

    private fun saveAll() {
        val jsonEpisodes = JSONArray()
        localEpisodes.forEach {
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

    private fun loadAll(): List<ReleaseFull.Episode> {
        val result = mutableListOf<ReleaseFull.Episode>()
        val savedEpisodes = sharedPreferences.getString(LOCAL_EPISODES_KEY, null)
        savedEpisodes?.let {
            val jsonEpisodes = JSONArray(it)
            (0 until jsonEpisodes.length()).forEach {
                jsonEpisodes.getJSONObject(it).let {
                    result.add(ReleaseFull.Episode().apply {
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