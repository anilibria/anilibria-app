package ru.radiationx.anilibria.ui.activities.main

import ru.radiationx.anilibria.ui.adapters.BottomTabListItem
import ru.radiationx.anilibria.ui.adapters.main.BottomTabDelegate
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter

/**
 * Created by radiationx on 25.02.18.
 */
class BottomTabsAdapter(
    private val clickListener: (MainTab) -> Unit,
    private val longClickListener: (MainTab) -> Unit
) : ListItemAdapter() {

    init {
        delegatesManager.run {
            addDelegate(BottomTabDelegate(clickListener, longClickListener))
        }
    }

    fun bindItems(tabsState: MainTabsState) {
        items = tabsState.tabs.map {
            BottomTabListItem(it, it == tabsState.selected)
        }
    }
}