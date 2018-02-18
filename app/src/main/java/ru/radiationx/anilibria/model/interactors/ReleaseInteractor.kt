package ru.radiationx.anilibria.model.interactors

import android.util.Log
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function
import ru.radiationx.anilibria.entity.app.release.ReleaseFull
import ru.radiationx.anilibria.model.data.storage.EpisodesCheckerStorage
import ru.radiationx.anilibria.model.repository.ReleaseRepository

/**
 * Created by radiationx on 17.02.18.
 */
class ReleaseInteractor(
        private val releaseRepository: ReleaseRepository,
        private val episodesCheckerStorage: EpisodesCheckerStorage
) {

    fun putEpisode(episode: ReleaseFull.Episode) = episodesCheckerStorage.putEpisode(episode)

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
                            //Log.e("SUKA", "BiFunction: ${t1.episodes.size}, ${t2.size}")
                            /*t1.episodes.forEach { episode ->
                                t2.filter { it.releaseId == episode.releaseId }.firstOrNull { it.id == episode.id }?.let {
                                    episode.apply {
                                        isViewed = it.isViewed
                                        seek = it.seek
                                    }
                                }
                            }*/

                            t2
                                    .filter {
                                        //Log.e("SUKA", "filter ${it.releaseId}==${t1.id}")
                                        it.releaseId == t1.id
                                    }
                                    .forEach { localEpisode ->

                                        t1.episodes.firstOrNull {
                                            //Log.e("SUKA", "first ORNULL ${it.id}==${localEpisode.id}")
                                            it.id == localEpisode.id
                                        }
                                                ?.let {
                                                    it.isViewed = localEpisode.isViewed
                                                    it.seek = localEpisode.seek
                                                    Log.e("SUKA", "Found, Set: ${it.isViewed}, ${it.seek}")
                                                }
                                    }
                            t1
                        }
                )
    }
}