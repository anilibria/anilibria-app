package ru.radiationx.data.apinext.datasources

import anilibria.api.videos.VideosApi
import ru.radiationx.data.apinext.toDomain
import ru.radiationx.data.entity.domain.youtube.YoutubeItem
import toothpick.InjectConstructor

@InjectConstructor
class VideosApiDataSource(
    private val api: VideosApi
) {

    suspend fun getVideos(limit: Int?): List<YoutubeItem> {
        return api.getVideos(limit).map { it.toDomain() }
    }
}