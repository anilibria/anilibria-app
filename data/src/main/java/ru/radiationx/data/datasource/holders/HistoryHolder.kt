package ru.radiationx.data.datasource.holders

import io.reactivex.Observable
import io.reactivex.Single
import ru.radiationx.data.entity.app.release.ReleaseItem

interface HistoryHolder {
    fun getEpisodes(): Single<List<ReleaseItem>>
    fun observeEpisodes(): Observable<MutableList<ReleaseItem>>
    fun putRelease(release: ReleaseItem)
    fun putAllRelease(releases: List<ReleaseItem>)
    fun removerRelease(id: Int)
}