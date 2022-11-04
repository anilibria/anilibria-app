package ru.radiationx.data.entity.app.release

import java.io.Serializable

/* Created by radiationx on 31.10.17. */

open class ReleaseItem : Serializable {
    var id: Int = -1
    var code: String? = null
    val names = mutableListOf<String>()
    var series: String? = null
    var poster: String? = null
    var torrentUpdate: Int = 0
    var status: String? = null
    var statusCode: String? = null
    val types = mutableListOf<String>()
    val genres = mutableListOf<String>()
    val voices = mutableListOf<String>()
    val seasons = mutableListOf<String>()
    val days = mutableListOf<String>()
    var description: String? = null
    var announce: String? = null
    var favoriteInfo = FavoriteInfo(0, false)

    var isNew: Boolean = false

    var link: String? = null

    val title: String?
        get() = names.firstOrNull()

    val titleEng: String?
        get() = names.lastOrNull()




    companion object {
        const val STATUS_CODE_PROGRESS = "1"
        const val STATUS_CODE_COMPLETE = "2"
        const val STATUS_CODE_HIDDEN = "3"
        const val STATUS_CODE_NOT_ONGOING = "4"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ReleaseItem) return false

        if (id != other.id) return false
        if (code != other.code) return false
        if (names != other.names) return false
        if (series != other.series) return false
        if (poster != other.poster) return false
        if (torrentUpdate != other.torrentUpdate) return false
        if (status != other.status) return false
        if (statusCode != other.statusCode) return false
        if (types != other.types) return false
        if (genres != other.genres) return false
        if (voices != other.voices) return false
        if (seasons != other.seasons) return false
        if (days != other.days) return false
        if (description != other.description) return false
        if (announce != other.announce) return false
        if (favoriteInfo != other.favoriteInfo) return false
        if (isNew != other.isNew) return false
        if (link != other.link) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (code?.hashCode() ?: 0)
        result = 31 * result + names.hashCode()
        result = 31 * result + (series?.hashCode() ?: 0)
        result = 31 * result + (poster?.hashCode() ?: 0)
        result = 31 * result + torrentUpdate
        result = 31 * result + (status?.hashCode() ?: 0)
        result = 31 * result + (statusCode?.hashCode() ?: 0)
        result = 31 * result + types.hashCode()
        result = 31 * result + genres.hashCode()
        result = 31 * result + voices.hashCode()
        result = 31 * result + seasons.hashCode()
        result = 31 * result + days.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (announce?.hashCode() ?: 0)
        result = 31 * result + favoriteInfo.hashCode()
        result = 31 * result + isNew.hashCode()
        result = 31 * result + (link?.hashCode() ?: 0)
        return result
    }
}
