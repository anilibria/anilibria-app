package ru.radiationx.data.datasource.storage

import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import ru.radiationx.data.DataPreferences
import ru.radiationx.data.datasource.holders.ReleaseUpdateHolder
import ru.radiationx.data.entity.db.ReleaseUpdateDb
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.release.ReleaseUpdate
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.entity.mapper.toDb
import ru.radiationx.data.entity.mapper.toDomain
import javax.inject.Inject

/**
 * Created by radiationx on 18.02.18.
 */
class ReleaseUpdateStorage @Inject constructor(
    @DataPreferences private val sharedPreferences: SharedPreferences,
    private val moshi: Moshi
) : ReleaseUpdateHolder {

    companion object {
        private const val LOCAL_HISTORY_KEY = "data.release_update"
    }

    private val dataAdapter by lazy {
        val type = Types.newParameterizedType(List::class.java, ReleaseUpdateDb::class.java)
        moshi.adapter<List<ReleaseUpdateDb>>(type)
    }

    private val localReleasesRelay by lazy {
        MutableStateFlow(loadAll())
    }

    override fun observeEpisodes(): Flow<List<ReleaseUpdate>> = localReleasesRelay

    override suspend fun getReleases(): List<ReleaseUpdate> =
        localReleasesRelay.value.toList()

    override fun getRelease(id: ReleaseId): ReleaseUpdate? {
        return localReleasesRelay.value.firstOrNull { it.id == id }
    }

    override fun viewRelease(release: Release) {
        getRelease(release.id)?.also { updItem ->
            val newUpdItem = updItem.copy(
                timestamp = release.torrentUpdate,
                lastOpenTimestamp = updItem.timestamp
            )
            updAllRelease(listOf(newUpdItem))
        }
    }

    override fun putInitialRelease(releases: List<Release>) {
        val putReleases = mutableListOf<ReleaseUpdate>()
        releases.forEach { release ->
            val updItem = getRelease(release.id)
            if (updItem == null) {
                val update = ReleaseUpdate(
                    id = release.id,
                    timestamp = release.torrentUpdate,
                    lastOpenTimestamp = Int.MAX_VALUE
                )
                putReleases.add(update)
            }
        }
        updAllRelease(putReleases)
    }

    private fun updAllRelease(releases: List<ReleaseUpdate>) {
        localReleasesRelay.update { updates ->
            val newIds = releases.map { it.id }
            updates
                .filterNot { newIds.contains(it.id) }
                .plus(releases)
        }
        saveAll()
    }

    private fun saveAll() {
        val jsonEpisodes = localReleasesRelay.value
            .map { it.toDb() }
            .let { dataAdapter.toJson(it) }
        sharedPreferences
            .edit()
            .putString(LOCAL_HISTORY_KEY, jsonEpisodes)
            .apply()
    }

    private fun loadAll(): List<ReleaseUpdate> = sharedPreferences
        .getString(LOCAL_HISTORY_KEY, null)
        ?.let { dataAdapter.fromJson(it) }
        ?.map { it.toDomain() }
        .orEmpty()
}