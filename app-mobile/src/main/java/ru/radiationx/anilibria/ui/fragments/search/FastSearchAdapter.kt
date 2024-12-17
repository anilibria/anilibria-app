package ru.radiationx.anilibria.ui.fragments.search

import ru.radiationx.anilibria.model.SuggestionItemState
import ru.radiationx.anilibria.model.SuggestionLocalItemState
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.LoadErrorListItem
import ru.radiationx.anilibria.ui.adapters.SuggestionListItem
import ru.radiationx.anilibria.ui.adapters.SuggestionLocalListItem
import ru.radiationx.anilibria.ui.adapters.global.LoadErrorDelegate
import ru.radiationx.anilibria.ui.adapters.search.SuggestionDelegate
import ru.radiationx.anilibria.ui.adapters.search.SuggestionLocalDelegate
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter
import ru.radiationx.shared_app.controllers.loadersingle.needShowError

/**
 * Created by radiationx on 24.12.17.
 */
class FastSearchAdapter(
    clickListener: (SuggestionItemState) -> Unit,
    localClickListener: (SuggestionLocalItemState) -> Unit,
    retryClickListener: () -> Unit
) : ListItemAdapter() {

    init {
        addDelegate(SuggestionDelegate(clickListener))
        addDelegate(SuggestionLocalDelegate(localClickListener))
        addDelegate(LoadErrorDelegate(retryClickListener))
    }

    fun bindItems(state: FastSearchScreenState) {
        val newItems = mutableListOf<ListItem>()
        state.loaderState.data?.also { data ->
            newItems.addAll(data.localItems.map { SuggestionLocalListItem(it) })
            newItems.addAll(data.items.map { SuggestionListItem(it) })
        }
        if (state.loaderState.needShowError()) {
            newItems.add(LoadErrorListItem("bottom"))
        }
        items = newItems
    }
}
