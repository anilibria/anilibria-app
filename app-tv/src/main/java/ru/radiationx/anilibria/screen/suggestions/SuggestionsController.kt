package ru.radiationx.anilibria.screen.suggestions

import com.jakewharton.rxrelay2.PublishRelay
import ru.radiationx.data.entity.app.search.SuggestionItem
import toothpick.InjectConstructor

@InjectConstructor
class SuggestionsController {

    val resultEvent = PublishRelay.create<SearchResult>()

    data class SearchResult(
        val items: List<SuggestionItem>,
        val query: String,
        val validQuery: Boolean
    )
}