package ru.radiationx.anilibria.ui.fragments.auth

import ru.radiationx.anilibria.ui.adapters.SocialAuthListItem
import ru.radiationx.anilibria.ui.adapters.auth.SocialAuthDelegate
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter
import ru.radiationx.data.entity.app.auth.SocialAuth

class SocialAuthAdapter(
    private val clickListener: (SocialAuth) -> Unit
) : ListItemAdapter() {

    init {
        delegatesManager.apply {
            addDelegate(SocialAuthDelegate(clickListener))
        }
    }

    fun bindItems(newItems: List<SocialAuth>) {
        items = newItems.map { SocialAuthListItem(it) }
    }
}
