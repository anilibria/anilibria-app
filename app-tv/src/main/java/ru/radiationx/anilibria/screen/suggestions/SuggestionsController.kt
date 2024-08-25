package ru.radiationx.anilibria.screen.suggestions

import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.shared.ktx.EventFlow
import javax.inject.Inject

class SuggestionsController @Inject constructor() {

    val resultEvent = EventFlow<SearchResult>()

    data class SearchResult(
        val items: List<Release>,
        val query: String,
        val validQuery: Boolean
    )
}