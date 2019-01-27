package ru.radiationx.anilibria.entity.app.youtube

class YoutubeItem {
    var id: Int = 0
    var title: String? = null
    var image: String? = null
    var vid: String? = null
    var views: Int = 0
    var comments: Int = 0
    var timestamp: Int = 0

    val link
        get() = "https://www.youtube.com/watch?v=$vid"
}