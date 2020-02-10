package ru.radiationx.anilibria.ui.fragments.auth

import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.SocialAuthListItem
import ru.radiationx.anilibria.ui.adapters.auth.SocialAuthDelegate
import ru.radiationx.data.entity.app.auth.SocialAuth

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
