package ru.radiationx.anilibria.model.data.storage

import android.content.SharedPreferences
import com.jakewharton.rxrelay2.BehaviorRelay
import com.mintrocket.gisdelivery.extension.nullString
import io.reactivex.Observable
import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.anilibria.entity.app.release.ReleaseFull
import ru.radiationx.anilibria.entity.app.release.ReleaseItem

/**
 * Created by radiationx on 18.02.18.
 */
class HistoryStorage(private val sharedPreferences: SharedPreferences) {

    companion object {
        private const val LOCAL_HISTORY_KEY = "data.local_history"
    }

    private val localReleases = mutableListOf<ReleaseItem>()
    private val localReleasesRelay = BehaviorRelay.createDefault(localReleases)

    init {
        loadAll()
    }

    fun observeEpisodes(): Observable<MutableList<ReleaseItem>> = localReleasesRelay

    fun putRelease(release: ReleaseItem) {
        localReleases
                .firstOrNull { it.id == release.id }
                ?.let { localReleases.remove(it) }
        localReleases.add(release)
        saveAll()
        localReleasesRelay.accept(localReleases)
    }

    private fun saveAll() {
        val jsonEpisodes = JSONArray()
        localReleases.forEach {
            jsonEpisodes.put(JSONObject().apply {
                put("id", it.id)
                put("idName", it.idName)
                put("title", it.title)
                put("originalTitle", it.originalTitle)
                put("torrentLink", it.torrentLink)
                put("link", it.link)
                put("image", it.image)
                put("episodesCount", it.episodesCount)
                put("description", it.description)
                put("seasons", JSONArray(it.seasons))
                put("voices", JSONArray(it.voices))
                put("genres", JSONArray(it.genres))
                put("types", JSONArray(it.types))
            })
        }
        sharedPreferences
                .edit()
                .putString(LOCAL_HISTORY_KEY, jsonEpisodes.toString())
                .apply()
    }

    private fun loadAll() {
        val savedEpisodes = sharedPreferences.getString(LOCAL_HISTORY_KEY, null)
        savedEpisodes?.let {
            val jsonEpisodes = JSONArray(it)
            (0 until jsonEpisodes.length()).forEach {
                jsonEpisodes.getJSONObject(it).let {
                    localReleases.add(ReleaseItem().apply {
                        id = it.getInt("id")
                        idName = it.nullString("idName")
                        title = it.nullString("title")
                        originalTitle = it.nullString("originalTitle")
                        torrentLink = it.nullString("torrentLink")
                        link = it.nullString("link")
                        image = it.nullString("image")
                        episodesCount = it.nullString("episodesCount")
                        description = it.nullString("description")
                        val jsonSeasons = it.getJSONArray("seasons")
                        (0 until jsonSeasons.length()).mapTo(seasons) { jsonSeasons.getString(it) }
                        val jsonVoices = it.getJSONArray("voices")
                        (0 until jsonVoices.length()).mapTo(voices) { jsonVoices.getString(it) }
                        val jsonGenres = it.getJSONArray("genres")
                        (0 until jsonGenres.length()).mapTo(genres) { jsonGenres.getString(it) }
                        val jsonTypes = it.getJSONArray("types")
                        (0 until jsonTypes.length()).mapTo(types) { jsonTypes.getString(it) }
                    })
                }
            }
        }
        localReleasesRelay.accept(localReleases)
    }
}