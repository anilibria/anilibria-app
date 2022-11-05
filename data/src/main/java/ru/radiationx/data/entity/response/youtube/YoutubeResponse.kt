package ru.radiationx.data.entity.response.youtube

data class YoutubeResponse(
    val id: Int,
    val title: String?,
    val image: String?,
    val vid: String?,
    val views: Int,
    val comments: Int,
    val timestamp: Int
) {

    val link = "https://www.youtube.com/watch?v=$vid"
}