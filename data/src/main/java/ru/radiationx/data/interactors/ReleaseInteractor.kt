package ru.radiationx.data.interactors

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import ru.radiationx.data.datasource.holders.EpisodesCheckerHolder
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.entity.app.Paginated
import ru.radiationx.data.entity.app.release.RandomRelease
import ru.radiationx.data.entity.app.release.ReleaseFull
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.repository.ReleaseRepository
import ru.radiationx.shared.ktx.repeatWhen
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

/**
 * Created by radiationx on 17.02.18.
 */
class ReleaseInteractor @Inject constructor(
    private val releaseRepository: ReleaseRepository,
    private val episodesCheckerStorage: EpisodesCheckerHolder,
    private val preferencesHolder: PreferencesHolder,
) {

    private val checkerCombiner: (suspend (ReleaseFull, List<ReleaseFull.Episode>) -> ReleaseFull) =
        { release, savedEpisodes ->
            val localEpisodes = savedEpisodes.filter { it.releaseId == release.id }
            release.episodes.forEach { newEpisode ->
                val localEpisode = localEpisodes.firstOrNull { it.id == newEpisode.id }
                newEpisode.isViewed = localEpisode?.isViewed ?: false
                newEpisode.seek = localEpisode?.seek ?: 0
                newEpisode.lastAccess = localEpisode?.lastAccess ?: 0
            }
            release
        }

    private val fullLoadCacheById = ConcurrentHashMap<Int, Flow<ReleaseFull>>()
    private val fullLoadCacheByCode = ConcurrentHashMap<String, Flow<ReleaseFull>>()

    private val releaseItemsById = mutableMapOf<Int, ReleaseItem>()
    private val releaseItemsByCode = mutableMapOf<String, ReleaseItem>()

    private val releasesById = mutableMapOf<Int, ReleaseFull>()
    private val releasesByCode = mutableMapOf<String, ReleaseFull>()

    private val itemsUpdateTrigger = MutableSharedFlow<Boolean>()
    private val fullUpdateTrigger = MutableSharedFlow<Boolean>()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    suspend fun getRandomRelease(): RandomRelease = releaseRepository.getRandomRelease()

    private fun loadRelease(releaseId: Int): Flow<ReleaseFull> {
        return flow { emit(releaseRepository.getRelease(releaseId)) }
            .onEach(this::updateFullCache)
            .onCompletion { fullLoadCacheById.remove(releaseId) }
            .shareIn(scope, SharingStarted.Eagerly, 1)
            .also { fullLoadCacheById[releaseId] = it }
    }

    private fun loadRelease(releaseCode: String): Flow<ReleaseFull> {
        return flow { emit(releaseRepository.getRelease(releaseCode)) }
            .onEach(this::updateFullCache)
            .onCompletion { fullLoadCacheByCode.remove(releaseCode) }
            .shareIn(scope, SharingStarted.Eagerly, 1)
            .also { fullLoadCacheByCode[releaseCode] = it }
    }

    fun loadRelease(releaseId: Int = -1, releaseCode: String? = null): Flow<ReleaseFull> {
        val releaseSource = releaseId.idOrNull()
            ?.let { fullLoadCacheById[it] }
            ?: releaseCode?.let { fullLoadCacheByCode[it] }
        (releaseSource)?.also {
            return it
        }

        return when {
            releaseId != -1 -> loadRelease(releaseId)
            releaseCode != null -> loadRelease(releaseCode)
            else -> flow { throw Exception("Unknown id=$releaseId or code=$releaseCode") }
        }
    }

    suspend fun loadReleases(page: Int): Paginated<List<ReleaseItem>> = releaseRepository
        .getReleases(page)
        .also { updateItemsCache(it.data) }

    fun getItem(releaseId: Int = -1, releaseCode: String? = null): ReleaseItem? {
        return releaseId.idOrNull()
            ?.let { releaseItemsById[it] }
            ?: releaseCode?.let { releaseItemsByCode[it] }
    }

    fun getFull(releaseId: Int = -1, releaseCode: String? = null): ReleaseFull? {
        return releaseId.idOrNull()
            ?.let { releasesById[it] }
            ?: releaseCode?.let { releasesByCode[it] }
    }

    fun observeItem(releaseId: Int = -1, releaseCode: String? = null): Flow<ReleaseItem> {
        return flowOf(true)
            .filter { getItem(releaseId, releaseCode) != null }
            .map { getItem(releaseId, releaseCode)!! }
            .repeatWhen(itemsUpdateTrigger)
    }

    fun observeFull(releaseId: Int = -1, releaseCode: String? = null): Flow<ReleaseFull> {
        return combine(
            createFullObservable(releaseId, releaseCode),
            episodesCheckerStorage.observeEpisodes(),
            checkerCombiner
        )
    }

    private fun createFullObservable(
        releaseId: Int = -1,
        releaseCode: String? = null
    ): Flow<ReleaseFull> {
        return flowOf(true)
            .filter { getFull(releaseId, releaseCode) != null }
            .map { getFull(releaseId, releaseCode)!! }
            .repeatWhen(fullUpdateTrigger)
    }

    suspend fun updateItemsCache(items: List<ReleaseItem>) {
        items.forEach { release ->
            releaseItemsById[release.id] = release
            release.code?.also { code ->
                releaseItemsByCode[code] = release
            }
        }
        itemsUpdateTrigger.emit(true)
    }

    suspend fun updateFullCache(release: ReleaseFull) {
        releasesById[release.id] = release
        release.code?.also { code ->
            releasesByCode[code] = release
        }
        fullUpdateTrigger.emit(true)
    }

    /* Common */
    fun putEpisode(episode: ReleaseFull.Episode) = episodesCheckerStorage.putEpisode(episode)

    fun putEpisodes(episodes: List<ReleaseFull.Episode>) =
        episodesCheckerStorage.putAllEpisode(episodes)

    fun getEpisodes(releaseId: Int) = episodesCheckerStorage.getEpisodes(releaseId)

    fun resetEpisodesHistory(releaseId: Int) {
        episodesCheckerStorage.remove(releaseId)
    }

    fun getQuality() = preferencesHolder.getQuality()

    fun setQuality(value: Int) = preferencesHolder.setQuality(value)

    fun observeQuality() = preferencesHolder.observeQuality()

    fun getPlayerType() = preferencesHolder.getPlayerType()

    fun setPlayerType(value: Int) = preferencesHolder.setPlayerType(value)

    fun getPlaySpeed() = preferencesHolder.playSpeed

    fun setPlaySpeed(value: Float) {
        preferencesHolder.playSpeed = value
    }

    fun observePlaySpeed(): Flow<Float> = preferencesHolder.observePlaySpeed()

    fun getPIPControl() = preferencesHolder.pipControl

    fun setPIPControl(value: Int) {
        preferencesHolder.pipControl = value
    }

    private fun Int.idOrNull(limit: Int = -1): Int? = if (this > limit) {
        this
    } else {
        null
    }
}