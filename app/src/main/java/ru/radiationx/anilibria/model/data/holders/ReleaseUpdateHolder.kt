package ru.radiationx.anilibria.model.data.holders

import io.reactivex.Observable
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.entity.app.release.ReleaseUpdate

interface ReleaseUpdateHolder {
    fun observeEpisodes(): Observable<MutableList<ReleaseUpdate>>
    fun getRelease(id: Int): ReleaseUpdate?
    fun updRelease(release: ReleaseUpdate)
    fun updAllRelease(releases: List<ReleaseUpdate>)
    fun putRelease(release: ReleaseItem)
    fun putAllRelease(releases: List<ReleaseItem>)
}