package ru.radiationx.anilibria.screen.suggestions

import ru.radiationx.data.entity.domain.search.SuggestionItem
import ru.radiationx.shared.ktx.EventFlow
import toothpick.InjectConstructor

@InjectConstructor
class SuggestionsController {

    val resultEvent = EventFlow<SearchResult>()

    data class SearchResult(
        val items: List<SuggestionItem>,
        val query: String,
        val validQuery: Boolean
    )
}