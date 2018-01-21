package ru.radiationx.anilibria.model.repository

import io.reactivex.Observable
import ru.radiationx.anilibria.entity.app.page.PageLibria
import ru.radiationx.anilibria.model.data.remote.api.PageApi
import ru.radiationx.anilibria.model.system.SchedulersProvider

/**
 * Created by radiationx on 13.01.18.
 */
class PageRepository(
        private val schedulers: SchedulersProvider,
        private val pageApi: PageApi
) {

    fun getPage(pageId: String): Observable<PageLibria> = pageApi
            .getPage(pageId)
            .toObservable()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

}
