package ru.radiationx.anilibria.ui.fragments.auth

import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import ru.radiationx.anilibria.entity.app.auth.SocialAuth
import ru.radiationx.anilibria.entity.app.search.SearchItem
import ru.radiationx.anilibria.entity.app.search.SuggestionItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.SearchListItem
import ru.radiationx.anilibria.ui.adapters.SearchSuggestionListItem
import ru.radiationx.anilibria.ui.adapters.SocialAuthListItem
import ru.radiationx.anilibria.ui.adapters.auth.SocialAuthDelegate
import ru.radiationx.anilibria.ui.adapters.search.SearchDelegate
import ru.radiationx.anilibria.ui.adapters.search.SearchSuggestionDelegate

class SocialAuthAdapter(
        private val clickListener: (SocialAuth) -> Unit
) : ListDelegationAdapter<MutableList<ListItem>>() {

    init {
        items = mutableListOf()
        delegatesManager.apply {
            addDelegate(SocialAuthDelegate(clickListener))
        }
    }

    fun bindItems(newItems: List<SocialAuth>) {
        items.clear()
        items.addAll(newItems.map { SocialAuthListItem(it) })
        notifyDataSetChanged()
    }

}
