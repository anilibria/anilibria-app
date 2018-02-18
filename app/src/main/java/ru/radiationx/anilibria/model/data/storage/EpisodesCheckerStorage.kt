package ru.radiationx.anilibria.model.data.storage

import android.content.SharedPreferences
import android.util.Log
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.anilibria.entity.app.release.ReleaseFull

/**
 * Created by radiationx on 17.02.18.
 */
class EpisodesCheckerStorage(private val sharedPreferences: SharedPreferences) {

    companion object {
        private const val LOCAL_EPISODES_KEY = "data.local_episodes"
    }

    private val localEpisodes = mutableListOf<ReleaseFull.Episode>()
    private val localEpisodesRelay = BehaviorRelay.createDefault(localEpisodes)

    init {
        loadAll()
    }

    fun observeEpisodes(): Observable<MutableList<ReleaseFull.Episode>> = localEpisodesRelay

    fun putEpisode(episode: ReleaseFull.Episode) {
        localEpisodes
                .firstOrNull { it.releaseId == episode.releaseId && it.id == episode.id }
                ?.let { localEpisodes.remove(it) }
        localEpisodes.add(episode)
        saveAll()
        localEpisodesRelay.accept(localEpisodes)
    }

    fun getEpisodes(releaseId: Int): List<ReleaseFull.Episode> {
        return localEpisodes.filter { it.releaseId == releaseId }
    }

    private fun saveAll() {
        val jsonEpisodes = JSONArray()
        localEpisodes.forEach {
            jsonEpisodes.put(JSONObject().apply {
                put("releaseId", it.releaseId)
                put("id", it.id)
                put("seek", it.seek)
                put("isViewed", it.isViewed)
            })
        }
        sharedPreferences
                .edit()
                .putString(LOCAL_EPISODES_KEY, jsonEpisodes.toString())
                .apply()
    }

    private fun loadAll() {
        val savedEpisodes = sharedPreferences.getString(LOCAL_EPISODES_KEY, null)
        savedEpisodes?.let {
            val jsonEpisodes = JSONArray(it)
            (0 until jsonEpisodes.length()).forEach {
                jsonEpisodes.getJSONObject(it).let {
                    localEpisodes.add(ReleaseFull.Episode().apply {
                        releaseId = it.getInt("releaseId")
                        id = it.getInt("id")
                        seek = it.getLong("seek")
                        isViewed = it.getBoolean("isViewed")
                    })
                }
            }
        }
        localEpisodesRelay.accept(localEpisodes)
    }
}