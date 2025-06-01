package ru.radiationx.data.app.releaseupdate

import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ru.radiationx.data.api.releases.models.Release
import ru.radiationx.data.app.releaseupdate.db.ReleaseUpdateDb
import ru.radiationx.data.app.releaseupdate.mapper.toDb
import ru.radiationx.data.app.releaseupdate.mapper.toDomain
import ru.radiationx.data.app.releaseupdate.models.ReleaseUpdate
import ru.radiationx.data.common.ReleaseId
import ru.radiationx.data.di.DataPreferences
import ru.radiationx.shared.ktx.android.SuspendMutableStateFlow
import java.util.Date
import javax.inject.Inject

/**
 * Created by radiationx on 18.02.18.
 */
class ReleaseUpdateStorage @Inject constructor(
    @DataPreferences private val sharedPreferences: SharedPreferences,
    private val moshi: Moshi,
) : ReleaseUpdateHolder {

    companion object {
        private const val LOCAL_HISTORY_KEY = "data.release_update"
    }

    private val dataAdapter by lazy {
        val type = Types.newParameterizedType(List::class.java, ReleaseUpdateDb::class.java)
        moshi.adapter<List<ReleaseUpdateDb>>(type)
    }

    private val localReleasesRelay = SuspendMutableStateFlow {
        loadAll()
    }

    override fun observeEpisodes(): Flow<List<ReleaseUpdate>> = localReleasesRelay

    override suspend fun getReleases(): List<ReleaseUpdate> =
        localReleasesRelay.getValue().toList()

    override suspend fun getRelease(id: ReleaseId): ReleaseUpdate? {
        return localReleasesRelay.getValue().firstOrNull { it.id == id }
    }

    override suspend fun viewRelease(release: Release) {
        getRelease(release.id)?.also { updItem ->
            val newUpdItem = updItem.copy(
                timestamp = release.freshAt,
                lastOpenTimestamp = updItem.timestamp
            )
            putAllRelease(listOf(newUpdItem))
        }
    }

    override suspend fun putInitialRelease(releases: List<Release>) {
        val putReleases = mutableListOf<ReleaseUpdate>()
        releases.forEach { release ->
            val updItem = getRelease(release.id)
            if (updItem == null) {
                val update = ReleaseUpdate(
                    id = release.id,
                    timestamp = release.freshAt,
                    lastOpenTimestamp = Date(Long.MAX_VALUE)
                )
                putReleases.add(update)
            }
        }
        putAllRelease(putReleases)
    }

    override suspend fun putAllRelease(releases: List<ReleaseUpdate>) {
        localReleasesRelay.update { updates ->
            val newIds = releases.map { it.id }
            updates
                .filterNot { newIds.contains(it.id) }
                .plus(releases)
        }
        saveAll()
    }

    private suspend fun saveAll() {
        withContext(Dispatchers.IO) {
            val jsonEpisodes = localReleasesRelay.getValue()
                .map { it.toDb() }
                .let { dataAdapter.toJson(it) }
            sharedPreferences
                .edit()
                .putString(LOCAL_HISTORY_KEY, jsonEpisodes)
                .apply()
        }
    }

    private suspend fun loadAll(): List<ReleaseUpdate> {
        return withContext(Dispatchers.IO) {
            sharedPreferences
                .getString(LOCAL_HISTORY_KEY, null)
                ?.let { dataAdapter.fromJson(it) }
                ?.map { it.toDomain() }
                .orEmpty()
        }
    }
}