package ru.radiationx.data.entity.domain.youtube

import ru.radiationx.data.entity.domain.types.YoutubeId
import java.util.Date

data class YoutubeItem(
    val id: YoutubeId,
    val title: String?,
    val image: String?,
    val vid: String,
    val link: String,
    val views: Int,
    val comments: Int,
    val createdAt: Date
)