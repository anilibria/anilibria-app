package ru.radiationx.data.datasource.storage

import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ru.radiationx.data.DataPreferences
import ru.radiationx.data.datasource.SuspendMutableStateFlow
import ru.radiationx.data.datasource.holders.EpisodesCheckerHolder
import ru.radiationx.data.entity.db.EpisodeAccessDb
import ru.radiationx.data.entity.db.EpisodeAccessLegacyDb
import ru.radiationx.data.entity.domain.release.EpisodeAccess
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.entity.mapper.toDb
import ru.radiationx.data.entity.mapper.toDomain
import javax.inject.Inject

/**
 * Created by radiationx on 17.02.18.
 */
class EpisodesCheckerStorage @Inject constructor(
    @DataPreferences private val sharedPreferences: SharedPreferences,
    private val moshi: Moshi,
) : EpisodesCheckerHolder {

    companion object {
        private const val LEGACY_LOCAL_EPISODES_KEY = "data.local_episodes"
        private const val LOCAL_EPISODES_KEY = "data.local_episodes_v2"
    }

    private val legacyDataAdapter by lazy {
        val type = Types.newParameterizedType(List::class.java, EpisodeAccessLegacyDb::class.java)
        moshi.adapter<List<EpisodeAccessLegacyDb>>(type)
    }

    private val dataAdapter by lazy {
        val type = Types.newParameterizedType(List::class.java, EpisodeAccessDb::class.java)
        moshi.adapter<List<EpisodeAccessDb>>(type)
    }

    private val localEpisodesRelay = SuspendMutableStateFlow {
        loadAll()
    }

    override fun observeEpisodes(): Flow<List<EpisodeAccess>> =
        localEpisodesRelay

    override suspend fun getEpisodes(): List<EpisodeAccess> {
        return localEpisodesRelay.getValue()
    }

    override suspend fun putEpisode(episode: EpisodeAccess) {
        localEpisodesRelay.update { localEpisodes ->
            val mutableLocalEpisodes = localEpisodes.toMutableList()
            mutableLocalEpisodes
                .firstOrNull { it.id == episode.id }
                ?.let { mutableLocalEpisodes.remove(it) }
            mutableLocalEpisodes.add(episode)
            mutableLocalEpisodes
        }
        saveAll()
    }

    override suspend fun putAllEpisode(episodes: List<EpisodeAccess>) {
        localEpisodesRelay.update { localEpisodes ->
            val mutableLocalEpisodes = localEpisodes.toMutableList()
            episodes.forEach { episode ->
                mutableLocalEpisodes
                    .firstOrNull { it.id == episode.id }
                    ?.let { mutableLocalEpisodes.remove(it) }
                mutableLocalEpisodes.add(episode)
            }
            mutableLocalEpisodes
        }
        saveAll()
    }

    override suspend fun getEpisodes(releaseId: ReleaseId): List<EpisodeAccess> {
        return localEpisodesRelay.getValue().filter { it.id.releaseId == releaseId }
    }

    override suspend fun getEpisode(episodeId: EpisodeId): EpisodeAccess? {
        return localEpisodesRelay.getValue().find { it.id == episodeId }
    }

    override suspend fun remove(releaseId: ReleaseId) {
        localEpisodesRelay.update { localEpisodes ->
            val mutableLocalEpisodes = localEpisodes.toMutableList()
            mutableLocalEpisodes.removeAll { it.id.releaseId == releaseId }
            mutableLocalEpisodes
        }
        saveAll()
    }

    private suspend fun saveAll() {
        withContext(Dispatchers.IO) {
            val jsonEpisodes = localEpisodesRelay.getValue()
                .map { it.toDb() }
                .let { dataAdapter.toJson(it) }
            sharedPreferences
                .edit()
                .putString(LOCAL_EPISODES_KEY, jsonEpisodes)
                .apply()
        }
    }

    private suspend fun loadAll(): List<EpisodeAccess> {
        return withContext(Dispatchers.IO) {
            val actualData = sharedPreferences
                .getString(LOCAL_EPISODES_KEY, null)
                ?.let { dataAdapter.fromJson(it) }
            val data = actualData ?: loadAllLegacy()?.map { it.toDb() }
            data?.map { it.toDomain() }.orEmpty()
        }
    }

    private suspend fun loadAllLegacy(): List<EpisodeAccessLegacyDb>? {
        return withContext(Dispatchers.IO) {
            sharedPreferences
                .getString(LEGACY_LOCAL_EPISODES_KEY, null)
                ?.let { legacyDataAdapter.fromJson(it) }
        }
    }
}