package ru.radiationx.data.datasource.storage

import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.data.DataPreferences
import ru.radiationx.data.datasource.SuspendMutableStateFlow
import ru.radiationx.data.datasource.holders.HistoryHolder
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.shared.ktx.android.mapObjects
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

    private val localReleasesRelay = SuspendMutableStateFlow {
        loadAll()
    }

    override suspend fun getIds() = localReleasesRelay.getValue()

    override fun observeIds(): Flow<List<ReleaseId>> = localReleasesRelay

    override suspend fun putId(id: ReleaseId) {
        localReleasesRelay.update { localReleases ->
            val mutableLocalReleases = localReleases.toMutableList()
            mutableLocalReleases
                .firstOrNull { it == id }
                ?.let { mutableLocalReleases.remove(it) }
            mutableLocalReleases.add(id)
            mutableLocalReleases
        }
        saveAll()
    }

    override suspend fun putAllIds(ids: List<ReleaseId>) {
        localReleasesRelay.update { localReleases ->
            val mutableLocalReleases = localReleases.toMutableList()
            ids.forEach { id ->
                mutableLocalReleases
                    .firstOrNull { it == id }
                    ?.let { mutableLocalReleases.remove(it) }
                mutableLocalReleases.add(id)
            }
            mutableLocalReleases
        }
        saveAll()
    }

    override suspend fun removeId(id: ReleaseId) {
        localReleasesRelay.update { localReleases ->
            val mutableLocalReleases = localReleases.toMutableList()
            mutableLocalReleases.firstOrNull { it == id }?.also {
                mutableLocalReleases.remove(it)
            }
            mutableLocalReleases
        }
        saveAll()
    }

    private suspend fun saveAll() {
        withContext(Dispatchers.IO) {
            val jsonEpisodes = JSONArray()
            localReleasesRelay.getValue().forEach {
                jsonEpisodes.put(JSONObject().apply {
                    put("id", it.id)
                })
            }
            sharedPreferences
                .edit()
                .putString(LOCAL_HISTORY_KEY, jsonEpisodes.toString())
                .apply()
        }
    }

    private suspend fun loadAll(): List<ReleaseId> {
        return withContext(Dispatchers.IO) {
            val result = mutableListOf<ReleaseId>()
            sharedPreferences
                .getString(LOCAL_HISTORY_KEY, null)
                ?.let { JSONArray(it) }
                ?.let { jsonEpisodes ->
                    jsonEpisodes.mapObjects { jsonRelease ->
                        val id = ReleaseId(jsonRelease.getInt("id"))
                        result.add(id)
                    }
                }
            result
        }
    }
}