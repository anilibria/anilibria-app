package ru.radiationx.anilibria.ui.fragments.search

import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.SearchListItem
import ru.radiationx.anilibria.ui.adapters.SearchSuggestionListItem
import ru.radiationx.anilibria.ui.adapters.search.SearchDelegate
import ru.radiationx.anilibria.ui.adapters.search.SearchSuggestionDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeAdapter
import ru.radiationx.data.entity.app.search.SearchItem
import ru.radiationx.data.entity.app.search.SuggestionItem

/**
 * Created by radiationx on 24.12.17.
 */
class FastSearchAdapter(
        clickListener: (SearchItem) -> Unit
) : OptimizeAdapter<MutableList<ListItem>>() {

    init {
        items = mutableListOf()
        addDelegate(SearchSuggestionDelegate(clickListener))
        addDelegate(SearchDelegate(clickListener))
    }

    fun bindItems(newItems: List<SearchItem>) {
        items.clear()
        items.addAll(newItems.map { item ->
            (item as? SuggestionItem)?.let {
                SearchSuggestionListItem(it)
            } ?: SearchListItem(item)
        })
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        val suggestionId = (items[position] as? SearchListItem)?.item?.id?.toLong()
        return suggestionId ?: position.toLong()
    }

}
