package ru.radiationx.anilibria.model.repository

import io.reactivex.Single
import ru.radiationx.anilibria.entity.app.Paginated
import ru.radiationx.anilibria.entity.app.youtube.YoutubeItem
import ru.radiationx.anilibria.model.data.remote.api.ReleaseApi
import ru.radiationx.anilibria.model.data.remote.api.YoutubeApi
import ru.radiationx.anilibria.model.system.SchedulersProvider

class YoutubeRepository(
        private val schedulers: SchedulersProvider,
        private val youtubeApi: YoutubeApi
) {

    fun getYoutubeList(page: Int): Single<Paginated<List<YoutubeItem>>> = youtubeApi
            .getYoutubeList(page)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
}