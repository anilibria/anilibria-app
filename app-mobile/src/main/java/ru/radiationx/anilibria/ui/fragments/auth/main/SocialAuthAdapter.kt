package ru.radiationx.anilibria.ui.fragments.auth.main

import ru.radiationx.anilibria.extension.setAndAwaitItems
import ru.radiationx.anilibria.model.SocialAuthItemState
import ru.radiationx.anilibria.ui.adapters.SocialAuthListItem
import ru.radiationx.anilibria.ui.adapters.auth.SocialAuthDelegate
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter

class SocialAuthAdapter(
    private val clickListener: (SocialAuthItemState) -> Unit
) : ListItemAdapter() {

    init {
        delegatesManager.apply {
            addDelegate(SocialAuthDelegate(clickListener))
        }
    }

    suspend fun bindItems(newItems: List<SocialAuthItemState>) {
        setAndAwaitItems(newItems.map { SocialAuthListItem(it) })
    }
}
