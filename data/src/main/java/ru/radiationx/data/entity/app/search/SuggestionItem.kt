package ru.radiationx.data.entity.app.search

class SuggestionItem : SearchItem() {
    var code: String? = null
    val names = mutableListOf<String>()
    var poster: String? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SuggestionItem) return false
        if (!super.equals(other)) return false

        if (code != other.code) return false
        if (names != other.names) return false
        if (poster != other.poster) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (code?.hashCode() ?: 0)
        result = 31 * result + names.hashCode()
        result = 31 * result + (poster?.hashCode() ?: 0)
        return result
    }


}