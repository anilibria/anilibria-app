package ru.radiationx.data.api.videos

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.radiationx.data.api.shared.pagination.Paginated
import ru.radiationx.data.api.videos.models.YoutubeItem
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