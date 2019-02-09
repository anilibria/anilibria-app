package ru.radiationx.anilibria.model.data.storage

import android.content.SharedPreferences
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.anilibria.di.qualifier.DataPreferences
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.entity.app.release.ReleaseUpdate
import ru.radiationx.anilibria.model.data.holders.ReleaseUpdateHolder
import ru.radiationx.anilibria.model.system.SchedulersProvider
import javax.inject.Inject

/**
 * Created by radiationx on 18.02.18.
 */
class ReleaseUpdateStorage @Inject constructor(
        @DataPreferences private val sharedPreferences: SharedPreferences,
        private val schedulers: SchedulersProvider
) : ReleaseUpdateHolder {

    companion object {
        private const val LOCAL_HISTORY_KEY = "data.release_update"
    }

    private val localReleases = mutableListOf<ReleaseUpdate>()
    private val localReleasesRelay = BehaviorRelay.createDefault(localReleases)

    init {
        loadAll()
    }

    override fun observeEpisodes(): Observable<MutableList<ReleaseUpdate>> = localReleasesRelay
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

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
        localReleasesRelay.accept(localReleases)
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
        localReleasesRelay.accept(localReleases)
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
            (0 until jsonEpisodes.length()).forEach {
                jsonEpisodes.getJSONObject(it).let {
                    localReleases.add(ReleaseUpdate().apply {
                        id = it.getInt("id")
                        timestamp = it.getInt("timestamp")
                        lastOpenTimestamp = it.getInt("lastOpenTimestamp")
                    })
                }
            }
        }
        localReleasesRelay.accept(localReleases)
    }
}