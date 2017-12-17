package ru.radiationx.anilibria.data.api.models

import java.io.Serializable
import java.util.*

/* Created by radiationx on 31.10.17. */

class ReleaseItem : Serializable {
    var id: Int = 0
    var title: String? = null
    var originalTitle: String? = null
    var torrentLink: String? = null
    var link: String? = null
    var image: String? = null
    var episodesCount: String? = null
    var description: String? = null
    val seasons: ArrayList<String> = ArrayList()
    val voices: ArrayList<String> = ArrayList()
    val genres: ArrayList<String> = ArrayList()
    val types: ArrayList<String> = ArrayList()

    /* Full Item */
    val episodes = ArrayList<Episode>()
    var moonwalkLink: String? = null

    class Episode : Serializable {
        var title: String? = null
        var urlSd: String? = null
        var urlHd: String? = null
    }
}
