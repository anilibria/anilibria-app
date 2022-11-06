package ru.radiationx.anilibria.screen.suggestions

import kotlinx.coroutines.flow.MutableSharedFlow
import ru.radiationx.data.entity.domain.search.SuggestionItem
import toothpick.InjectConstructor

@InjectConstructor
class SuggestionsController {

    val resultEvent = MutableSharedFlow<SearchResult>()

    data class SearchResult(
        val items: List<SuggestionItem>,
        val query: String,
        val validQuery: Boolean
    )
}