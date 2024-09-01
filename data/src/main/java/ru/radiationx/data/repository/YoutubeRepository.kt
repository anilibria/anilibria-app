package ru.radiationx.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.radiationx.data.apinext.datasources.VideosApiDataSource
import ru.radiationx.data.entity.domain.Paginated
import ru.radiationx.data.entity.domain.youtube.YoutubeItem
import javax.inject.Inject

class YoutubeRepository @Inject constructor(
    private val videosApi: VideosApiDataSource
) {

    // todo API2 await paginated api method
    suspend fun getYoutubeList(page: Int): Paginated<YoutubeItem> = withContext(Dispatchers.IO) {
        val videos = videosApi.getVideos(20)
        Paginated(videos, page, 1, 20, videos.size)
    }
}