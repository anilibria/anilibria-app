package ru.radiationx.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.radiationx.data.apinext.datasources.VideosApiDataSource
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.entity.domain.Paginated
import ru.radiationx.data.entity.domain.youtube.YoutubeItem
import ru.radiationx.data.system.ApiUtils
import javax.inject.Inject

class YoutubeRepository @Inject constructor(
    private val videosApi: VideosApiDataSource,
    private val apiUtils: ApiUtils,
    private val apiConfig: ApiConfig
) {

    suspend fun getYoutubeList(page: Int): Paginated<YoutubeItem> = withContext(Dispatchers.IO) {
        val videos = videosApi.getVideos(20)
        Paginated(videos, page, 1, 20, videos.size)
    }
}