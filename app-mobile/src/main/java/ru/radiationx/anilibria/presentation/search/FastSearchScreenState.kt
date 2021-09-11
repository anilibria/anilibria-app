package ru.radiationx.anilibria.presentation.search

import ru.radiationx.anilibria.model.SuggestionItemState
import ru.radiationx.anilibria.model.SuggestionLocalItemState

data class FastSearchScreenState(
    val loading: Boolean = false,
    val localItems: List<SuggestionLocalItemState> = emptyList(),
    val items: List<SuggestionItemState> = emptyList()
)
