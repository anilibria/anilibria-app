package ru.radiationx.data.api.videos.models

import ru.radiationx.data.common.Url
import ru.radiationx.data.common.YoutubeId
import java.util.Date

data class YoutubeItem(
    val id: YoutubeId,
    val title: String?,
    val image: Url.Path?,
    val vid: String,
    val link: Url.Absolute,
    val views: Int?,
    val comments: Int?,
    val createdAt: Date
)