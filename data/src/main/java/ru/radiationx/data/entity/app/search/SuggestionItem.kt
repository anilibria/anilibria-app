package ru.radiationx.data.entity.app.search

class SuggestionItem : SearchItem() {
    var code: String? = null
    val names = mutableListOf<String>()
    var poster: String? = null
}