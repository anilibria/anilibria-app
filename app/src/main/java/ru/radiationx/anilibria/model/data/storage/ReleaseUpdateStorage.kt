package ru.radiationx.anilibria.model.data.storage

import android.content.SharedPreferences
import com.jakewharton.rxrelay2.BehaviorRelay
import com.mintrocket.gisdelivery.extension.nullString
import io.reactivex.Observable
import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.entity.app.release.ReleaseUpdate
import ru.radiationx.anilibria.model.data.holders.HistoryHolder
import ru.radiationx.anilibria.model.data.holders.ReleaseUpdateHolder

/**
 * Created by radiationx on 18.02.18.
 */
class ReleaseUpdateStorage(private val sharedPreferences: SharedPreferences) : ReleaseUpdateHolder {

    companion object {
        private const val LOCAL_HISTORY_KEY = "data.release_update"
    }

    private val localReleases = mutableListOf<ReleaseUpdate>()
    private val localReleasesRelay = BehaviorRelay.createDefault(localReleases)

    init {
        loadAll()
    }

    override fun observeEpisodes(): Observable<MutableList<ReleaseUpdate>> = localReleasesRelay

    override fun getRelease(id: Int): ReleaseUpdate? {
        return localReleases.firstOrNull { it.id == id }
    }

    override fun putRelease(release: ReleaseItem) {
        localReleases
                .firstOrNull { it.id == release.id }
                ?.let { localReleases.remove(it) }
        localReleases.add(ReleaseUpdate().apply {
            id = release.id
            timestamp = release.torrentUpdate
        })
        saveAll()
        localReleasesRelay.accept(localReleases)
    }

    override fun putAllRelease(releases: List<ReleaseItem>) {
        releases.forEach { release ->
            localReleases
                    .firstOrNull { it.id == release.id }
                    ?.let { localReleases.remove(it) }
            localReleases.add(ReleaseUpdate().apply {
                id = release.id
                timestamp = release.torrentUpdate
            })
        }
        saveAll()
        localReleasesRelay.accept(localReleases)
    }

    private fun saveAll() {
        val jsonEpisodes = JSONArray()
        localReleases.forEach {
            jsonEpisodes.put(JSONObject().apply {
                put("id", it.id)
                put("timestamp", it.timestamp)
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
                    localReleases.add(ReleaseUpdate().apply {
                        id = it.getInt("id")
                        timestamp = it.getInt("timestamp")
                    })
                }
            }
        }
        localReleasesRelay.accept(localReleases)
    }
}