package ru.radiationx.anilibria.entity.app.release

import java.io.Serializable

/* Created by radiationx on 31.10.17. */

open class ReleaseItem : Serializable {
    var id: Int = 0
    var title: String? = null
    var originalTitle: String? = null
    var torrentLink: String? = null
    var link: String? = null
    var image: String? = null
    var episodesCount: String? = null
    var description: String? = null
    val seasons = mutableListOf<String>()
    val voices = mutableListOf<String>()
    val genres = mutableListOf<String>()
    val types = mutableListOf<String>()
}
