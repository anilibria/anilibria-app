package ru.radiationx.data.datasource.storage

import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.data.DataPreferences
import ru.radiationx.data.datasource.holders.HistoryHolder
import ru.radiationx.data.entity.app.release.FavoriteInfo
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.shared.ktx.android.nullString
import javax.inject.Inject

/**
 * Created by radiationx on 18.02.18.
 */
class HistoryStorage @Inject constructor(
    @DataPreferences private val sharedPreferences: SharedPreferences
) : HistoryHolder {

    companion object {
        private const val LOCAL_HISTORY_KEY = "data.local_history_new"
    }

    private val localReleasesRelay by lazy {
        MutableStateFlow(loadAll())
    }

    override suspend fun getEpisodes() = localReleasesRelay.value

    override fun observeEpisodes(): Flow<List<ReleaseItem>> = localReleasesRelay

    override fun putRelease(release: ReleaseItem) {
        localReleasesRelay.update { localReleases ->
            val mutableLocalReleases = localReleases.toMutableList()
            mutableLocalReleases
                .firstOrNull { it.id == release.id }
                ?.let { mutableLocalReleases.remove(it) }
            mutableLocalReleases.add(release)
            mutableLocalReleases
        }
        saveAll()
    }

    override fun putAllRelease(releases: List<ReleaseItem>) {
        localReleasesRelay.update { localReleases ->
            val mutableLocalReleases = localReleases.toMutableList()
            releases.forEach { release ->
                mutableLocalReleases
                    .firstOrNull { it.id == release.id }
                    ?.let { mutableLocalReleases.remove(it) }
                mutableLocalReleases.add(release)
            }
            mutableLocalReleases
        }
        saveAll()
    }

    override fun removerRelease(id: Int) {
        localReleasesRelay.update { localReleases ->
            val mutableLocalReleases = localReleases.toMutableList()
            mutableLocalReleases.firstOrNull { it.id == id }?.also {
                mutableLocalReleases.remove(it)
                localReleasesRelay.value = mutableLocalReleases.toList()
            }
            mutableLocalReleases
        }
        saveAll()
    }

    private fun saveAll() {
        val jsonEpisodes = JSONArray()
        localReleasesRelay.value.forEach {
            jsonEpisodes.put(JSONObject().apply {
                put("id", it.id)
                put("code", it.code)
                put("link", it.link)
                put("names", JSONArray(it.names))
                put("series", it.series)
                put("poster", it.poster)
                put("torrentUpdate", it.torrentUpdate)
                put("status", it.status)
                put("statusCode", it.statusCode)
                put("announce", it.announce)
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

    private fun loadAll(): List<ReleaseItem> {
        val result = mutableListOf<ReleaseItem>()
        val jsonEpisodes =
            sharedPreferences.getString(LOCAL_HISTORY_KEY, null)?.let { JSONArray(it) }
        if (jsonEpisodes != null) {
            (0 until jsonEpisodes.length()).forEach { releaseIndex ->
                val jsonRelease = jsonEpisodes.getJSONObject(releaseIndex)
                val release = ReleaseItem().apply {
                    id = jsonRelease.getInt("id")
                    code = jsonRelease.nullString("code")
                    link = jsonRelease.nullString("link")
                    jsonRelease.getJSONArray("names").also { jsonNames ->
                        (0 until jsonNames.length()).mapTo(names) { jsonNames.getString(it) }
                    }
                    series = jsonRelease.nullString("series")
                    poster = jsonRelease.nullString("poster")
                    torrentUpdate = jsonRelease.getInt("torrentUpdate")
                    status = jsonRelease.nullString("status")
                    statusCode = jsonRelease.nullString("statusCode")
                    announce = jsonRelease.nullString("announce")
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
                        favoriteInfo = FavoriteInfo(
                            rating = jsonFav.getInt("rating"),
                            isAdded = jsonFav.getBoolean("isAdded")
                        )
                    }
                }
                result.add(release)
            }
        }
        return result
    }
}