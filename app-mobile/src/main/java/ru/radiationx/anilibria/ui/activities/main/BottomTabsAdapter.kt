package ru.radiationx.anilibria.ui.activities.main

import ru.radiationx.anilibria.ui.adapters.BottomTabListItem
import ru.radiationx.anilibria.ui.adapters.main.BottomTabDelegate
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter

/**
 * Created by radiationx on 25.02.18.
 */
class BottomTabsAdapter(
    private val listener: BottomTabDelegate.Listener
) : ListItemAdapter() {

    private var currentScreenKey: String? = null

    init {
        delegatesManager.run {
            addDelegate(BottomTabDelegate(listener))
        }
    }

    fun bindItems(tabs: List<MainActivity.Tab>) {
        items = tabs.map {
            BottomTabListItem(it, it.screen.screenKey == currentScreenKey)
        }
    }

    fun setSelected(screenKey: String) {
        currentScreenKey = screenKey
        items = items.map {
            if (it is BottomTabListItem) {
                it.copy(selected = it.item.screen.screenKey == screenKey)
            } else {
                it
            }
        }
    }

    interface Listener : BottomTabDelegate.Listener
}