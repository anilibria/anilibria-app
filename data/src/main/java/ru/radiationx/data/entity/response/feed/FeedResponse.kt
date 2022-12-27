package ru.radiationx.data.entity.response.feed

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import ru.radiationx.data.entity.response.release.ReleaseResponse
import ru.radiationx.data.entity.response.youtube.YoutubeResponse

@JsonClass(generateAdapter = true)
data class FeedResponse(
    @Json(name = "release") val release: ReleaseResponse? = null,
    @Json(name = "youtube") val youtube: YoutubeResponse? = null
)