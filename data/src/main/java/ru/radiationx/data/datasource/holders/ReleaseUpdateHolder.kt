package ru.radiationx.data.datasource.holders

import io.reactivex.Observable
import io.reactivex.Single
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.entity.app.release.ReleaseUpdate

interface ReleaseUpdateHolder {
    fun observeEpisodes(): Observable<MutableList<ReleaseUpdate>>
    fun getReleases(): Single<List<ReleaseUpdate>>
    fun getRelease(id: Int): ReleaseUpdate?
    fun updRelease(release: ReleaseUpdate)
    fun updAllRelease(releases: List<ReleaseUpdate>)
    fun putRelease(release: ReleaseItem)
    fun putAllRelease(releases: List<ReleaseItem>)
}