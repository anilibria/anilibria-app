package ru.radiationx.data.repository

import io.reactivex.Single
import ru.radiationx.data.SchedulersProvider
import ru.radiationx.data.datasource.remote.api.YoutubeApi
import ru.radiationx.data.entity.app.Paginated
import ru.radiationx.data.entity.app.youtube.YoutubeItem
import javax.inject.Inject

class YoutubeRepository @Inject constructor(
        private val schedulers: SchedulersProvider,
        private val youtubeApi: YoutubeApi
) {

    fun getYoutubeList(page: Int): Single<Paginated<List<YoutubeItem>>> = youtubeApi
            .getYoutubeList(page)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
}