package ru.radiationx.data.entity.app.youtube

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is YoutubeItem) return false

        if (id != other.id) return false
        if (title != other.title) return false
        if (image != other.image) return false
        if (vid != other.vid) return false
        if (views != other.views) return false
        if (comments != other.comments) return false
        if (timestamp != other.timestamp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (image?.hashCode() ?: 0)
        result = 31 * result + (vid?.hashCode() ?: 0)
        result = 31 * result + views
        result = 31 * result + comments
        result = 31 * result + timestamp
        return result
    }


}