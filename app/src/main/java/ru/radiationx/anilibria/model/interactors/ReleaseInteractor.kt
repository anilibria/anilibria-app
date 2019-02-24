package ru.radiationx.anilibria.model.interactors

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import ru.radiationx.anilibria.entity.app.Paginated
import ru.radiationx.anilibria.entity.app.release.ReleaseFull
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.extension.idOrNull
import ru.radiationx.anilibria.model.data.holders.EpisodesCheckerHolder
import ru.radiationx.anilibria.model.data.holders.PreferencesHolder
import ru.radiationx.anilibria.model.repository.ReleaseRepository
import ru.radiationx.anilibria.model.system.SchedulersProvider
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
        savedEpisodes.filter { it.releaseId == release.id }.forEach { localEpisode ->
            release.episodes.firstOrNull { it.id == localEpisode.id }?.also {
                it.isViewed = localEpisode.isViewed
                it.seek = localEpisode.seek
                it.lastAccess = localEpisode.lastAccess
            }
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

    fun observeItem(releaseId: Int = -1, releaseCode: String? = null): Observable<ReleaseItem> = itemsUpdateTrigger
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

    private fun createFullObservable(releaseId: Int = -1, releaseCode: String? = null) = fullUpdateTrigger
            .filter { getFull(releaseId, releaseCode) != null }
            .map { getFull(releaseId, releaseCode)!! }
            .repeatWhen { fullUpdateTrigger }

    private fun updateItemsCache(items: List<ReleaseItem>) {
        items.forEach { release ->
            releaseItemsById[release.id] = release
            release.code?.also { code ->
                releaseItemsByCode[code] = release
            }
        }
        itemsUpdateTrigger.accept(true)
    }

    private fun updateFullCache(release: ReleaseFull) {
        releasesById[release.id] = release
        release.code?.also { code ->
            releasesByCode[code] = release
        }
        fullUpdateTrigger.accept(true)
    }

    /* Common */
    fun putEpisode(episode: ReleaseFull.Episode) = episodesCheckerStorage.putEpisode(episode)

    fun getEpisodes(releaseId: Int) = episodesCheckerStorage.getEpisodes(releaseId)

    fun getQuality() = preferencesHolder.getQuality()

    fun setQuality(value: Int) = preferencesHolder.setQuality(value)

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
}