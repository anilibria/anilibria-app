package ru.radiationx.data.interactors

import android.util.Log
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import ru.radiationx.data.SchedulersProvider
import ru.radiationx.data.datasource.holders.EpisodesCheckerHolder
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.entity.app.Paginated
import ru.radiationx.data.entity.app.release.RandomRelease
import ru.radiationx.data.entity.app.release.ReleaseFull
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.repository.ReleaseRepository
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

/**
 * Created by radiationx on 17.02.18.
 */
class ReleaseInteractor @Inject constructor(
    private val releaseRepository: ReleaseRepository,
    private val episodesCheckerStorage: EpisodesCheckerHolder,
    private val preferencesHolder: PreferencesHolder,
    private val schedulers: SchedulersProvider
) {

    private val checkerCombiner = BiFunction<ReleaseFull, List<ReleaseFull.Episode>, ReleaseFull> { release, savedEpisodes ->
        val localEpisodes = savedEpisodes.filter { it.releaseId == release.id }
        release.episodes.forEach { newEpisode ->
            val localEpisode = localEpisodes.firstOrNull { it.id == newEpisode.id }
            newEpisode.isViewed = localEpisode?.isViewed ?: false
            newEpisode.seek = localEpisode?.seek ?: 0
            newEpisode.lastAccess = localEpisode?.lastAccess ?: 0
        }
        release
    }

    private val fullLoadCacheById = ConcurrentHashMap<Int, Observable<ReleaseFull>>()
    private val fullLoadCacheByCode = ConcurrentHashMap<String, Observable<ReleaseFull>>()

    private val releaseItemsById = mutableMapOf<Int, ReleaseItem>()
    private val releaseItemsByCode = mutableMapOf<String, ReleaseItem>()

    private val releasesById = mutableMapOf<Int, ReleaseFull>()
    private val releasesByCode = mutableMapOf<String, ReleaseFull>()

    private val itemsUpdateTrigger = PublishRelay.create<Boolean>()
    private val fullUpdateTrigger = PublishRelay.create<Boolean>()

    fun getRandomRelease(): Single<RandomRelease> = releaseRepository.getRandomRelease()

    private fun loadRelease(releaseId: Int): Observable<ReleaseFull> = releaseRepository
        .getRelease(releaseId)
        .doOnSuccess(this::updateFullCache)
        .doFinally { fullLoadCacheById.remove(releaseId) }
        .toObservable()
        .share()
        .replay()
        .autoConnect(1)
        .also { fullLoadCacheById[releaseId] = it }

    private fun loadRelease(releaseCode: String): Observable<ReleaseFull> = releaseRepository
        .getRelease(releaseCode)
        .doOnSuccess(this::updateFullCache)
        .doFinally { fullLoadCacheByCode.remove(releaseCode) }
        .toObservable()
        .share()
        .replay()
        .autoConnect(1)
        .also { fullLoadCacheByCode[releaseCode] = it }

    fun loadRelease(releaseId: Int = -1, releaseCode: String? = null): Observable<ReleaseFull> {
        val releaseSource = releaseId.idOrNull()
            ?.let { fullLoadCacheById[it] }
            ?: releaseCode?.let { fullLoadCacheByCode[it] }
        (releaseSource)?.also {
            return it
        }
        return when {
            releaseId != -1 -> loadRelease(releaseId)
            releaseCode != null -> loadRelease(releaseCode)
            else -> Observable.error(Exception("Unknown id=$releaseId or code=$releaseCode"))
        }
    }

    fun loadReleases(page: Int): Single<Paginated<List<ReleaseItem>>> = releaseRepository
        .getReleases(page)
        .doOnSuccess { updateItemsCache(it.data) }

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

    fun observeItem(releaseId: Int = -1, releaseCode: String? = null): Observable<ReleaseItem> = Observable
        .just(true)
        .filter { getItem(releaseId, releaseCode) != null }
        .map { getItem(releaseId, releaseCode)!! }
        .repeatWhen { itemsUpdateTrigger }

    fun observeFull(releaseId: Int = -1, releaseCode: String? = null): Observable<ReleaseFull> = Observable
        .combineLatest(
            createFullObservable(releaseId, releaseCode),
            episodesCheckerStorage.observeEpisodes(),
            checkerCombiner
        )
        .subscribeOn(schedulers.io())
        .observeOn(schedulers.ui())

    private fun createFullObservable(releaseId: Int = -1, releaseCode: String? = null) = Observable
        .just(true)
        .filter { getFull(releaseId, releaseCode) != null }
        .map { getFull(releaseId, releaseCode)!! }
        .repeatWhen { fullUpdateTrigger }

    fun updateItemsCache(items: List<ReleaseItem>) {
        Log.e("kekeke", "updateItemsCache ${items.size}")
        items.forEach { release ->
            releaseItemsById[release.id] = release
            release.code?.also { code ->
                releaseItemsByCode[code] = release
            }
        }
        itemsUpdateTrigger.accept(true)
    }

    fun updateFullCache(release: ReleaseFull) {
        releasesById[release.id] = release
        release.code?.also { code ->
            releasesByCode[code] = release
        }
        fullUpdateTrigger.accept(true)
    }

    /* Common */
    fun putEpisode(episode: ReleaseFull.Episode) = episodesCheckerStorage.putEpisode(episode)

    fun putEpisodes(episodes: List<ReleaseFull.Episode>) = episodesCheckerStorage.putAllEpisode(episodes)

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