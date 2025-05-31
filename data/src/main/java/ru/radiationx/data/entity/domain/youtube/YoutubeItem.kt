package ru.radiationx.data.entity.domain.youtube

import ru.radiationx.data.entity.common.Url
import ru.radiationx.data.entity.domain.types.YoutubeId
import java.util.Date

data class YoutubeItem(
    val id: YoutubeId,
    val title: String?,
    val image: Url.Relative?,
    val vid: String,
    val link: Url.Absolute,
    val views: Int,
    val comments: Int,
    val createdAt: Date
)