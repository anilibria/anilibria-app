package ru.radiationx.anilibria.model.data.storage

import android.content.SharedPreferences
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.anilibria.di.qualifier.DataPreferences
import ru.radiationx.anilibria.entity.app.release.ReleaseFull
import ru.radiationx.anilibria.model.data.holders.EpisodesCheckerHolder
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

    private val localEpisodes = mutableListOf<ReleaseFull.Episode>()
    private val localEpisodesRelay = BehaviorRelay.createDefault(localEpisodes)

    init {
        loadAll()
    }

    override fun observeEpisodes(): Observable<MutableList<ReleaseFull.Episode>> = localEpisodesRelay

    override fun putEpisode(episode: ReleaseFull.Episode) {
        episode.lastAccess = System.currentTimeMillis()
        localEpisodes
                .firstOrNull { it.releaseId == episode.releaseId && it.id == episode.id }
                ?.let { localEpisodes.remove(it) }
        localEpisodes.add(episode)
        saveAll()
        localEpisodesRelay.accept(localEpisodes)
    }

    override fun putAllEpisode(episodes: List<ReleaseFull.Episode>) {
        episodes.forEach { episode ->
            episode.lastAccess = System.currentTimeMillis()
            localEpisodes
                    .firstOrNull { it.releaseId == episode.releaseId && it.id == episode.id }
                    ?.let { localEpisodes.remove(it) }
            localEpisodes.add(episode)
        }
        saveAll()
        localEpisodesRelay.accept(localEpisodes)
    }

    override fun getEpisodes(releaseId: Int): List<ReleaseFull.Episode> {
        return localEpisodes.filter { it.releaseId == releaseId }
    }

    override fun remove(releaseId: Int) {
        localEpisodes.removeAll { it.releaseId == releaseId }
        saveAll()
        localEpisodesRelay.accept(localEpisodes)
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

    private fun loadAll() {
        val savedEpisodes = sharedPreferences.getString(LOCAL_EPISODES_KEY, null)
        savedEpisodes?.let {
            val jsonEpisodes = JSONArray(it)
            (0 until jsonEpisodes.length()).forEach {
                jsonEpisodes.getJSONObject(it).let {
                    localEpisodes.add(ReleaseFull.Episode().apply {
                        releaseId = it.getInt("releaseId")
                        id = it.getInt("id")
                        seek = it.optLong("seek", 0L)
                        isViewed = it.optBoolean("isViewed", false)
                        lastAccess = it.optLong("lastAccess", 0L)
                    })
                }
            }
        }
        localEpisodesRelay.accept(localEpisodes)
    }
}