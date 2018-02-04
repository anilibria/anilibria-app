package ru.radiationx.anilibria.entity.app.release

import java.io.Serializable

class ReleaseFull() : ReleaseItem(), Serializable {

    constructor(item: ReleaseItem) : this() {
        id = item.id
        idName = item.idName
        title = item.title
        originalTitle = item.originalTitle
        torrentLink = item.torrentLink
        link = item.link
        image = item.image
        episodesCount = item.episodesCount
        description = item.description
        seasons.addAll(item.seasons)
        voices.addAll(item.voices)
        genres.addAll(item.genres)
        types.addAll(item.types)
    }

    var isBlocked = false
    var contentBlocked: String? = null
    var releaseStatus: String? = null
    val torrents = mutableListOf<TorrentItem>()
    val favoriteCount = FavoriteCount()
    val episodes = mutableListOf<Episode>()
    val episodesSource = mutableListOf<Episode>()
    var moonwalkLink: String? = null

    class Episode : Serializable {
        var title: String? = null
        var urlSd: String? = null
        var urlHd: String? = null
        lateinit var type: Type

        enum class Type : Serializable {
            ONLINE, SOURCE
        }
    }
}
