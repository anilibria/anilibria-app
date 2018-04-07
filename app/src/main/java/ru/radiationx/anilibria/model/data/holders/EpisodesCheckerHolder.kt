package ru.radiationx.anilibria.model.data.holders

import io.reactivex.Observable
import ru.radiationx.anilibria.entity.app.release.ReleaseFull

interface EpisodesCheckerHolder {
    fun observeEpisodes(): Observable<MutableList<ReleaseFull.Episode>>
    fun putEpisode(episode: ReleaseFull.Episode)
    fun getEpisodes(releaseId: Int): List<ReleaseFull.Episode>
}