package ru.radiationx.anilibria.model.repository

import io.reactivex.Single
import ru.radiationx.anilibria.entity.app.Paginated
import ru.radiationx.anilibria.entity.app.feed.FeedItem
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.model.data.remote.api.FeedApi
import ru.radiationx.anilibria.model.system.SchedulersProvider
import javax.inject.Inject

class FeedRepository @Inject constructor(
        private val feedApi: FeedApi,
        private val schedulers: SchedulersProvider
){

    fun getFeed(page: Int): Single<List<FeedItem>> = feedApi
            .getFeed(page)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

}