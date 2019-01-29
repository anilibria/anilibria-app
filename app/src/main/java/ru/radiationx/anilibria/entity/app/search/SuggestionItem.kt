package ru.radiationx.anilibria.entity.app.search

class SuggestionItem : SearchItem() {
    var code: String? = null
    val names = mutableListOf<String>()
    var poster: String? = null
}