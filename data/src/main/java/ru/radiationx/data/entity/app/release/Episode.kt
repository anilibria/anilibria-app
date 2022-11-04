package ru.radiationx.data.entity.app.release

import java.io.Serializable
import java.util.*

class Episode : Serializable {
    var releaseId = 0
    var id: Int = 0
    var seek: Long = 0
    var isViewed: Boolean = false
    var lastAccess: Long = 0

    var title: String? = null
    var urlSd: String? = null
    var urlHd: String? = null
    var urlFullHd: String? = null
    var updatedAt: Date? = null
    var skips: PlayerSkips? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Episode) return false

        if (releaseId != other.releaseId) return false
        if (id != other.id) return false
        if (seek != other.seek) return false
        if (isViewed != other.isViewed) return false
        if (lastAccess != other.lastAccess) return false
        if (title != other.title) return false
        if (urlSd != other.urlSd) return false
        if (urlHd != other.urlHd) return false
        if (urlFullHd != other.urlFullHd) return false
        if (updatedAt != other.updatedAt) return false
        if (skips != other.skips) return false

        return true
    }

    override fun hashCode(): Int {
        var result = releaseId
        result = 31 * result + id
        result = 31 * result + seek.hashCode()
        result = 31 * result + isViewed.hashCode()
        result = 31 * result + lastAccess.hashCode()
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (urlSd?.hashCode() ?: 0)
        result = 31 * result + (urlHd?.hashCode() ?: 0)
        result = 31 * result + (urlFullHd?.hashCode() ?: 0)
        result = 31 * result + (updatedAt?.hashCode() ?: 0)
        result = 31 * result + (skips?.hashCode() ?: 0)
        return result
    }


}