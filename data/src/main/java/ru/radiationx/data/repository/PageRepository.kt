package ru.radiationx.data.repository

import io.reactivex.Single
import ru.radiationx.data.SchedulersProvider
import ru.radiationx.data.datasource.remote.api.PageApi
import ru.radiationx.data.entity.app.page.PageLibria
import ru.radiationx.data.entity.app.page.VkComments
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
