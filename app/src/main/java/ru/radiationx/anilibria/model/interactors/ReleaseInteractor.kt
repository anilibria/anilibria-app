package ru.radiationx.anilibria.model.interactors

import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import ru.radiationx.anilibria.entity.app.release.ReleaseFull
import ru.radiationx.anilibria.model.data.holders.EpisodesCheckerHolder
import ru.radiationx.anilibria.model.data.holders.PreferencesHolder
import ru.radiationx.anilibria.model.repository.ReleaseRepository
import ru.radiationx.anilibria.model.system.SchedulersProvider

/**
 * Created by radiationx on 17.02.18.
 */
class ReleaseInteractor(
        private val releaseRepository: ReleaseRepository,
        private val episodesCheckerStorage: EpisodesCheckerHolder,
        private val preferencesHolder: PreferencesHolder,
        private val schedulers: SchedulersProvider
) {

    fun putEpisode(episode: ReleaseFull.Episode) = episodesCheckerStorage.putEpisode(episode)

    fun getEpisodes(releaseId: Int) = episodesCheckerStorage.getEpisodes(releaseId)

    fun getQuality() = preferencesHolder.getQuality()

    fun setQuality(value: Int) = preferencesHolder.setQuality(value)

    fun observeRelease(id: Int, idCode: String?): Observable<ReleaseFull> {
        val source = when {
            id != -1 -> releaseRepository.getRelease(id)
            idCode != null -> releaseRepository.getRelease(idCode)
            else -> return Observable.error(Exception("Wtf brou?!"))
        }
        return Observable
                .combineLatest(
                        source,
                        episodesCheckerStorage.observeEpisodes(),
                        BiFunction<ReleaseFull, List<ReleaseFull.Episode>, ReleaseFull> { t1, t2 ->
                            t2.filter { it.releaseId == t1.id }.forEach { localEpisode ->
                                t1.episodes.firstOrNull { it.id == localEpisode.id }?.let {
                                    it.isViewed = localEpisode.isViewed
                                    it.seek = localEpisode.seek
                                    it.lastAccess = localEpisode.lastAccess
                                }
                            }
                            t1
                        }
                )
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
    }
}