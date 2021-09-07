package ru.radiationx.data.entity.app.release

import java.io.Serializable

/**
 * Created by radiationx on 30.01.18.
 */
class TorrentItem : Serializable {
    var id: Int = 0
    var hash: String? = null
    var leechers: Int = 0
    var seeders: Int = 0
    var completed: Int = 0
    var quality: String? = null
    var series: String? = null
    var size: Long = 0
    var url: String? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TorrentItem) return false

        if (id != other.id) return false
        if (hash != other.hash) return false
        if (leechers != other.leechers) return false
        if (seeders != other.seeders) return false
        if (completed != other.completed) return false
        if (quality != other.quality) return false
        if (series != other.series) return false
        if (size != other.size) return false
        if (url != other.url) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (hash?.hashCode() ?: 0)
        result = 31 * result + leechers
        result = 31 * result + seeders
        result = 31 * result + completed
        result = 31 * result + (quality?.hashCode() ?: 0)
        result = 31 * result + (series?.hashCode() ?: 0)
        result = 31 * result + size.hashCode()
        result = 31 * result + (url?.hashCode() ?: 0)
        return result
    }

}