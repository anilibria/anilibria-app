package ru.radiationx.data.api.videos.mapper

import anilibria.api.videos.models.VideoResponse
import ru.radiationx.data.api.shared.apiDateToDate
import ru.radiationx.data.api.videos.models.YoutubeItem
import ru.radiationx.data.common.YoutubeId
import ru.radiationx.data.common.toAbsoluteUrl
import ru.radiationx.data.common.toRelativeUrl

fun VideoResponse.toDomain(): YoutubeItem {
    return YoutubeItem(
        id = YoutubeId(id = id),
        title = title,
        image = image.preview?.toRelativeUrl(),
        vid = videoId,
        link = url.toAbsoluteUrl(),
        views = views,
        comments = comments,
        createdAt = createdAt.apiDateToDate()
    )
}