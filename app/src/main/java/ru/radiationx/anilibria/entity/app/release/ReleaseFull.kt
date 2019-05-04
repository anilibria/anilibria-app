package ru.radiationx.anilibria.entity.app.release

import java.io.Serializable

class ReleaseFull() : ReleaseItem(), Serializable {

    constructor(item: ReleaseItem) : this() {
        id = item.id
        code = item.code
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
    }
}
