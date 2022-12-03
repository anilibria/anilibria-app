package ru.radiationx.anilibria.ui.fragments.search

import ru.radiationx.anilibria.model.SuggestionItemState
import ru.radiationx.anilibria.model.SuggestionLocalItemState
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.SuggestionListItem
import ru.radiationx.anilibria.ui.adapters.SuggestionLocalListItem
import ru.radiationx.anilibria.ui.adapters.search.SuggestionDelegate
import ru.radiationx.anilibria.ui.adapters.search.SuggestionLocalDelegate
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter

/**
 * Created by radiationx on 24.12.17.
 */
class FastSearchAdapter(
    clickListener: (SuggestionItemState) -> Unit,
    localClickListener: (SuggestionLocalItemState) -> Unit
) : ListItemAdapter() {

    init {
        addDelegate(SuggestionDelegate(clickListener))
        addDelegate(SuggestionLocalDelegate(localClickListener))
    }

    fun bindItems(state: FastSearchScreenState) {
        val newItems = mutableListOf<ListItem>()

        newItems.addAll(state.localItems.map { SuggestionLocalListItem(it) })
        newItems.addAll(state.items.map { SuggestionListItem(it) })

        items = newItems
    }
}
