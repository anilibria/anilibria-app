package ru.radiationx.data.entity.domain.youtube

import ru.radiationx.data.entity.domain.types.YoutubeId

data class YoutubeItem(
    val id: YoutubeId,
    val title: String?,
    val image: String?,
    val vid: String?,
    val views: Int,
    val comments: Int,
    val timestamp: Int
) {

    val link = "https://www.youtube.com/watch?v=$vid"
}