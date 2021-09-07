package ru.radiationx.data.entity.app.search

/**
 * Created by radiationx on 24.12.17.
 */
open class SearchItem {
    var id: Int = 0
    var icRes: Int = 0
    var title: String? = null
    var query: String? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SearchItem) return false

        if (id != other.id) return false
        if (icRes != other.icRes) return false
        if (title != other.title) return false
        if (query != other.query) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + icRes
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (query?.hashCode() ?: 0)
        return result
    }

}
