package ru.radiationx.anilibria.model.datasource.holders

import io.reactivex.Observable
import ru.radiationx.data.entity.app.release.ReleaseItem

interface HistoryHolder {
    fun observeEpisodes(): Observable<MutableList<ReleaseItem>>
    fun putRelease(release: ReleaseItem)
    fun putAllRelease(releases: List<ReleaseItem>)
    fun removerRelease(id: Int)
}