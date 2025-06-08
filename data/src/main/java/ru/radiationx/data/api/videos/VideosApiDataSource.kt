package ru.radiationx.data.api.videos

import anilibria.api.videos.VideosApi
import ru.radiationx.data.api.videos.mapper.toDomain
import ru.radiationx.data.api.videos.models.YoutubeItem
import javax.inject.Inject

class VideosApiDataSource @Inject constructor(
    private val api: VideosApi
) {

    suspend fun getVideos(limit: Int?): List<YoutubeItem> {
        return api.getVideos(limit).map { it.toDomain() }
    }
}