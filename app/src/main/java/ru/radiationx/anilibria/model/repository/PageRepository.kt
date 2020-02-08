package ru.radiationx.anilibria.model.repository

import io.reactivex.Single
import ru.radiationx.data.entity.app.page.PageLibria
import ru.radiationx.data.entity.app.page.VkComments
import ru.radiationx.anilibria.model.data.remote.api.PageApi
import ru.radiationx.anilibria.model.system.SchedulersProvider
import javax.inject.Inject

/**
 * Created by radiationx on 13.01.18.
 */
class PageRepository @Inject constructor(
        private val schedulers: SchedulersProvider,
        private val pageApi: PageApi
) {

    fun getPage(pagePath: String): Single<PageLibria> = pageApi
            .getPage(pagePath)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun getComments(): Single<VkComments> = pageApi
            .getComments()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

}
