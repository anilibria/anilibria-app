package ru.radiationx.anilibria.screen.search

import com.jakewharton.rxrelay2.PublishRelay
import ru.radiationx.data.entity.app.search.SuggestionItem
import toothpick.InjectConstructor

@InjectConstructor
class SearchController {

    val resultEvent = PublishRelay.create<SearchResult>()

    data class SearchResult(
        val items: List<SuggestionItem>,
        val query: String,
        val validQuery: Boolean
    )
}