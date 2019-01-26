package ru.radiationx.anilibria.model.data.storage

import android.content.SharedPreferences
import com.jakewharton.rxrelay2.BehaviorRelay
import ru.radiationx.anilibria.extension.nullString
import io.reactivex.Observable
import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.model.data.holders.HistoryHolder

/**
 * Created by radiationx on 18.02.18.
 */
class HistoryStorage(private val sharedPreferences: SharedPreferences) : HistoryHolder {

    companion object {
        private const val LOCAL_HISTORY_KEY = "data.local_history"
    }

    private val localReleases = mutableListOf<ReleaseItem>()
    private val localReleasesRelay = BehaviorRelay.createDefault(localReleases)

    init {
        loadAll()
    }

    override fun observeEpisodes(): Observable<MutableList<ReleaseItem>> = localReleasesRelay

    override fun putRelease(release: ReleaseItem) {
        localReleases
                .firstOrNull { it.id == release.id }
                ?.let { localReleases.remove(it) }
        localReleases.add(release)
        saveAll()
        localReleasesRelay.accept(localReleases)
    }

    override fun putAllRelease(releases: List<ReleaseItem>) {
        releases.forEach { release ->
            localReleases
                    .firstOrNull { it.id == release.id }
                    ?.let { localReleases.remove(it) }
            localReleases.add(release)
        }
        saveAll()
        localReleasesRelay.accept(localReleases)
    }

    override fun removerRelease(id: Int) {
        localReleases.firstOrNull { it.id == id }?.also {
            localReleases.remove(it)
            localReleasesRelay.accept(localReleases)
        }
    }

    private fun saveAll() {
        val jsonEpisodes = JSONArray()
        localReleases.forEach {
            jsonEpisodes.put(JSONObject().apply {
                put("id", it.id)
                put("code", it.code)
                put("names", JSONArray(it.names))
                put("series", it.series)
                put("poster", it.poster)
                put("posterFull", it.posterFull)
                put("torrentUpdate", it.torrentUpdate)
                put("status", it.status)
                put("types", JSONArray(it.types))
                put("genres", JSONArray(it.genres))
                put("voices", JSONArray(it.voices))
                put("seasons", JSONArray(it.seasons))
                put("days", JSONArray(it.days))
                put("description", it.description)
                put("favoriteInfo", it.favoriteInfo.let { favInfo ->
                    JSONObject().apply {
                        put("rating", favInfo.rating)
                        put("isAdded", favInfo.isAdded)
                    }
                })
            })
        }
        sharedPreferences
                .edit()
                .putString(LOCAL_HISTORY_KEY, jsonEpisodes.toString())
                .apply()
    }

    private fun loadAll() {
        val jsonEpisodes = sharedPreferences.getString(LOCAL_HISTORY_KEY, null)?.let { JSONArray(it) }
        if (jsonEpisodes != null) {
            (0 until jsonEpisodes.length()).forEach { releaseIndex ->
                val jsonRelease = jsonEpisodes.getJSONObject(releaseIndex)
                val release = ReleaseItem().apply {
                    id = jsonRelease.getInt("id")
                    code = jsonRelease.nullString("code")
                    jsonRelease.getJSONArray("names").also { jsonNames ->
                        (0 until jsonNames.length()).mapTo(names) { jsonNames.getString(it) }
                    }
                    series = jsonRelease.nullString("series")
                    poster = jsonRelease.nullString("poster")
                    posterFull = jsonRelease.nullString("posterFull")
                    torrentUpdate = jsonRelease.getInt("torrentUpdate")
                    status = jsonRelease.nullString("status")
                    jsonRelease.getJSONArray("types").also { jsonTypes ->
                        (0 until jsonTypes.length()).mapTo(types) { jsonTypes.getString(it) }
                    }
                    jsonRelease.getJSONArray("genres").also { jsonGenres ->
                        (0 until jsonGenres.length()).mapTo(genres) { jsonGenres.getString(it) }
                    }
                    jsonRelease.getJSONArray("voices").also { jsonVoices ->
                        (0 until jsonVoices.length()).mapTo(voices) { jsonVoices.getString(it) }
                    }
                    jsonRelease.getJSONArray("seasons").also { jsonSeasons ->
                        (0 until jsonSeasons.length()).mapTo(seasons) { jsonSeasons.getString(it) }
                    }
                    jsonRelease.getJSONArray("days").also { jsonDays ->
                        (0 until jsonDays.length()).mapTo(days) { jsonDays.getString(it) }
                    }
                    description = jsonRelease.nullString("description")
                    jsonRelease.getJSONObject("favoriteInfo").also { jsonFav ->
                        favoriteInfo.rating = jsonFav.getInt("rating")
                        favoriteInfo.isAdded = jsonFav.getBoolean("isAdded")
                    }
                }
                localReleases.add(release)
            }
        }
        localReleasesRelay.accept(localReleases)
    }
}