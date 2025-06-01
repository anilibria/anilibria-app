package ru.radiationx.data.api.videos

import anilibria.api.videos.VideosApi
import ru.radiationx.data.api.videos.mapper.toDomain
import ru.radiationx.data.api.videos.models.YoutubeItem
import toothpick.InjectConstructor

@InjectConstructor
class VideosApiDataSource(
    private val api: VideosApi
) {

    suspend fun getVideos(limit: Int?): List<YoutubeItem> {
        return api.getVideos(limit).map { it.toDomain() }
    }
}