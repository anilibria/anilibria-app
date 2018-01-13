package ru.radiationx.anilibria.ui.fragments.search

import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import ru.radiationx.anilibria.entity.app.search.SearchItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.SearchSuggestionListItem
import ru.radiationx.anilibria.ui.adapters.search.SearchSuggestionDelegate

/**
 * Created by radiationx on 24.12.17.
 */
class FastSearchAdapter : ListDelegationAdapter<MutableList<ListItem>>() {

    init {
        items = mutableListOf()
        delegatesManager.run {
            addDelegate(SearchSuggestionDelegate())
        }
    }

    fun bindItems(newItems: List<SearchItem>) {
        items.clear()
        items.addAll(newItems.map { SearchSuggestionListItem(it) })
    }

}
