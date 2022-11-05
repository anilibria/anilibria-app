package ru.radiationx.data.repository

import ru.radiationx.data.datasource.remote.api.YoutubeApi
import ru.radiationx.data.entity.app.Paginated
import ru.radiationx.data.entity.app.youtube.YoutubeItem
import javax.inject.Inject

class YoutubeRepository @Inject constructor(
    private val youtubeApi: YoutubeApi
) {

    suspend fun getYoutubeList(page: Int): Paginated<YoutubeItem> = youtubeApi
        .getYoutubeList(page)
}