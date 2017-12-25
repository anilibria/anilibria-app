package ru.radiationx.anilibria.entity.app.release

import java.io.Serializable

class ReleaseFull() : ReleaseItem(), Serializable {

    constructor(item: ReleaseItem) : this() {
        id = item.id
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

    val episodes = mutableListOf<Episode>()
    var moonwalkLink: String? = null

    class Episode : Serializable {
        var title: String? = null
        var urlSd: String? = null
        var urlHd: String? = null
    }
}
