package ru.radiationx.data.datasource.storage

import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.data.DataPreferences
import ru.radiationx.data.datasource.holders.ReleaseUpdateHolder
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.entity.app.release.ReleaseUpdate
import javax.inject.Inject

/**
 * Created by radiationx on 18.02.18.
 */
class ReleaseUpdateStorage @Inject constructor(
    @DataPreferences private val sharedPreferences: SharedPreferences,
) : ReleaseUpdateHolder {

    companion object {
        private const val LOCAL_HISTORY_KEY = "data.release_update"
    }

    private val localReleases = mutableListOf<ReleaseUpdate>()
    private val localReleasesRelay = MutableStateFlow(localReleases.toList())

    init {
        loadAll()
    }

    override fun observeEpisodes(): Flow<List<ReleaseUpdate>> = localReleasesRelay

    override suspend fun getReleases(): List<ReleaseUpdate> =
        localReleasesRelay.value.toList()

    override fun getRelease(id: Int): ReleaseUpdate? {
        return localReleases.firstOrNull { it.id == id }
    }

    override fun updRelease(release: ReleaseUpdate) {
        updAllRelease(listOf(release))
    }

    override fun updAllRelease(releases: List<ReleaseUpdate>) {
        releases.forEach { release ->
            localReleases
                .firstOrNull { it.id == release.id }
                ?.let { localReleases.remove(it) }
            localReleases.add(ReleaseUpdate().apply {
                id = release.id
                timestamp = release.timestamp
                lastOpenTimestamp = release.lastOpenTimestamp
            })
        }
        saveAll()
        localReleasesRelay.value = localReleases.toList()
    }

    override fun putRelease(release: ReleaseItem) {
        putAllRelease(listOf(release))
    }

    override fun putAllRelease(releases: List<ReleaseItem>) {
        releases.forEach { release ->
            localReleases
                .firstOrNull { it.id == release.id }
                ?.let { localReleases.remove(it) }
            localReleases.add(ReleaseUpdate().apply {
                id = release.id
                timestamp = release.torrentUpdate
                //lastOpenTimestamp = release.torrentUpdate
            })
        }
        saveAll()
        localReleasesRelay.value = localReleases.toList()
    }

    private fun saveAll() {
        val jsonEpisodes = JSONArray()
        localReleases.forEach {
            jsonEpisodes.put(JSONObject().apply {
                put("id", it.id)
                put("timestamp", it.timestamp)
                put("lastOpenTimestamp", it.lastOpenTimestamp)
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
            (0 until jsonEpisodes.length()).forEach { index ->
                jsonEpisodes.getJSONObject(index).let {
                    localReleases.add(ReleaseUpdate().apply {
                        id = it.getInt("id")
                        timestamp = it.getInt("timestamp")
                        lastOpenTimestamp = it.getInt("lastOpenTimestamp")
                    })
                }
            }
        }
        localReleasesRelay.value = localReleases.toList()
    }
}