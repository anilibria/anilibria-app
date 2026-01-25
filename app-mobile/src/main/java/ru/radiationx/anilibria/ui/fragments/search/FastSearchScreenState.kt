package ru.radiationx.anilibria.ui.fragments.search

import ru.radiationx.anilibria.model.SuggestionItemState
import ru.radiationx.anilibria.model.SuggestionLocalItemState
import ru.radiationx.shared_app.controllers.loadersingle.SingleLoaderState

data class FastSearchScreenState(
    val loaderState: SingleLoaderState<FastSearchDataState> = SingleLoaderState.empty()
)

data class FastSearchDataState(
    val localItems: List<SuggestionLocalItemState> = emptyList(),
    val items: List<SuggestionItemState> = emptyList()
)
