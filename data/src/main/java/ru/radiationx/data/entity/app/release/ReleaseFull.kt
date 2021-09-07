package ru.radiationx.data.entity.app.release

import java.io.Serializable

class ReleaseFull() : ReleaseItem(), Serializable {

    constructor(item: ReleaseItem) : this() {
        id = item.id
        code = item.code
        link = item.link
        names.addAll(item.names)
        series = item.series
        poster = item.poster
        torrentUpdate = item.torrentUpdate
        status = item.status
        statusCode = item.statusCode
        announce = item.announce
        types.addAll(item.types)
        genres.addAll(item.genres)
        voices.addAll(item.voices)
        seasons.addAll(item.seasons)
        days.addAll(item.days)
        description = item.description
        favoriteInfo.also {
            it.rating = item.favoriteInfo.rating
            it.isAdded = item.favoriteInfo.isAdded
        }

        isNew = item.isNew
    }

    var showDonateDialog: Boolean = false

    val blockedInfo = BlockedInfo()

    var moonwalkLink: String? = null
    val episodes = mutableListOf<Episode>()
    val episodesSource = mutableListOf<Episode>()

    val torrents = mutableListOf<TorrentItem>()

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
        lateinit var type: Type

        enum class Type : Serializable {
            ONLINE, SOURCE
        }

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
            if (type != other.type) return false

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
            result = 31 * result + type.hashCode()
            return result
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ReleaseFull) return false
        if (!super.equals(other)) return false

        if (showDonateDialog != other.showDonateDialog) return false
        if (blockedInfo != other.blockedInfo) return false
        if (moonwalkLink != other.moonwalkLink) return false
        if (episodes != other.episodes) return false
        if (episodesSource != other.episodesSource) return false
        if (torrents != other.torrents) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + showDonateDialog.hashCode()
        result = 31 * result + blockedInfo.hashCode()
        result = 31 * result + (moonwalkLink?.hashCode() ?: 0)
        result = 31 * result + episodes.hashCode()
        result = 31 * result + episodesSource.hashCode()
        result = 31 * result + torrents.hashCode()
        return result
    }


}
