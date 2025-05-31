package ru.radiationx.data.apinext

import anilibria.api.videos.models.VideoResponse
import ru.radiationx.data.entity.domain.types.YoutubeId
import ru.radiationx.data.entity.domain.youtube.YoutubeItem

fun VideoResponse.toDomain(): YoutubeItem {
    return YoutubeItem(
        id = YoutubeId(id = id),
        title = title,
        image = image.preview?.toRelativeUrl(),
        vid = videoId,
        link = url,
        views = views,
        comments = comments,
        createdAt = createdAt.apiDateToDate()
    )
}