package ru.radiationx.data.entity.response.feed

import ru.radiationx.data.entity.response.release.ReleaseResponse
import ru.radiationx.data.entity.response.youtube.YoutubeResponse

data class FeedItemResponse(
    val release: ReleaseResponse? = null,
    val youtube: YoutubeResponse? = null
)