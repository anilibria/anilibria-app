package ru.radiationx.anilibria.entity.app.release

import ru.radiationx.anilibria.model.data.remote.Api
import java.io.Serializable

/* Created by radiationx on 31.10.17. */

open class ReleaseItem : Serializable {
    var id: Int = -1
    var code: String? = null
    val names = mutableListOf<String>()
    var series: String? = null
    var poster: String? = null
    var posterFull: String? = null
    var torrentUpdate: Int = 0
    var status: String? = null
    val types = mutableListOf<String>()
    val genres = mutableListOf<String>()
    val voices = mutableListOf<String>()
    val seasons = mutableListOf<String>()
    val days = mutableListOf<String>()
    var description: String? = null
    val favoriteInfo = FavoriteInfo()

    var isNew: Boolean = false

    val link: String?
        get() = code?.let { "${Api.SITE_URL}/release/$it.html" }

    val title: String?
        get() = names.firstOrNull()

    val titleEng: String?
        get() = names.lastOrNull()
}
