package ru.radiationx.data.repository

import io.reactivex.Single
import ru.radiationx.data.MainClient
import ru.radiationx.data.SchedulersProvider
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.api.PageApi
import ru.radiationx.data.entity.app.page.PageLibria
import ru.radiationx.data.entity.app.page.VkComments
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by radiationx on 13.01.18.
 */
class PageRepository @Inject constructor(
    @MainClient private val mainClient: IClient,
    private val schedulers: SchedulersProvider,
    private val pageApi: PageApi
) {

    private var currentComments: VkComments? = null

    fun getPage(pagePath: String): Single<PageLibria> = pageApi
        .getPage(pagePath)
        .subscribeOn(schedulers.io())
        .observeOn(schedulers.ui())

    fun getComments(): Single<VkComments> = Single
        .defer {
            val comments = currentComments
            if (comments == null) {
                pageApi.getComments()
            } else {
                Single.just(comments)
            }
        }
        .subscribeOn(schedulers.io())
        .observeOn(schedulers.ui())

    fun checkVkBlocked(): Single<Boolean> = mainClient
        .get("https://vk.com/", emptyMap())
        .subscribeOn(schedulers.io())
        .observeOn(schedulers.ui())
        .timeout(15, TimeUnit.SECONDS)
        .map { false }
        .onErrorReturn { it !is UnknownHostException }

}
