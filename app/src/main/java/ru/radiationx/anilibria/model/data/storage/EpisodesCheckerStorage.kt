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

    private val localEpisodes = mutableListOf<ReleaseFull.Episode>()
    private val localEpisodesRelay = BehaviorRelay.createDefault(localEpisodes)

    init {
        loadAll()
    }

    fun observeEpisodes(): Observable<MutableList<ReleaseFull.Episode>> = localEpisodesRelay

    fun putEpisode(episode: ReleaseFull.Episode) {
        Log.e("SUKA", "put episode ${episode.releaseId}, ${episode.id}, ${episode.isViewed}")
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
        Log.e("SUKA", "Save episodes src: ${jsonEpisodes.toString()}")
        sharedPreferences
                .edit()
                .putString("keklol", jsonEpisodes.toString())
                .apply()
    }

    private fun loadAll() {
        val savedEpisodes = sharedPreferences.getString("keklol", null)
        Log.e("SUKA", "Saved episodes src: $savedEpisodes")
        savedEpisodes?.let {
            val jsonEpisodes = JSONArray(it)
            (0 until jsonEpisodes.length()).forEach {
                jsonEpisodes.getJSONObject(it).let {
                    localEpisodes.add(ReleaseFull.Episode().apply {
                        releaseId = it.getInt("releaseId")
                        id = it.getInt("id")
                        seek = it.getLong("seek")
                        isViewed = it.getBoolean("isViewed")
                        Log.e("SUKA", "Loaded episode: $releaseId, $id, $seek, $isViewed")
                    })
                }
            }
        }
        localEpisodesRelay.accept(localEpisodes)
    }
}