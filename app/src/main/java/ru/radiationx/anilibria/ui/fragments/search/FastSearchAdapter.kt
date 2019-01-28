package ru.radiationx.anilibria.ui.fragments.search

import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.search.FastSearchItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.SearchSimpleListItem
import ru.radiationx.anilibria.ui.adapters.SearchSuggestionListItem
import ru.radiationx.anilibria.ui.adapters.search.SearchSimpleDelegate
import ru.radiationx.anilibria.ui.adapters.search.SearchSuggestionDelegate

/**
 * Created by radiationx on 24.12.17.
 */
class FastSearchAdapter : ListDelegationAdapter<MutableList<ListItem>>() {

    init {
        items = mutableListOf()
        delegatesManager.apply {
            addDelegate(SearchSuggestionDelegate())
            addDelegate(SearchSimpleDelegate())
        }
    }

    fun bindItems(newItems: List<FastSearchItem>, query: String) {
        items.clear()
        items.addAll(newItems.map { SearchSuggestionListItem(it, query) })
        if (newItems.isEmpty() && query.isNotEmpty()) {
            items.add(SearchSimpleListItem(R.drawable.ic_toolbar_search,"Искать по жанрам и годам", query))
            items.add(SearchSimpleListItem(R.drawable.ic_google,"Найти в гугле \"$query\"", query))
        }
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        val suggestionId = (items.get(position) as? SearchSuggestionListItem)?.item?.id?.toLong()
        val simpleId = (items.get(position) as? SearchSimpleListItem)?.title?.hashCode()?.toLong()
        return suggestionId ?: simpleId ?: position.toLong()
    }

}
