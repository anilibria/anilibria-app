package ru.radiationx.anilibria.ui.activities.main

import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import ru.radiationx.anilibria.ui.adapters.BottomTabListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.main.BottomTabDelegate

/**
 * Created by radiationx on 25.02.18.
 */
class BottomTabsAdapter(private val listener: BottomTabDelegate.Listener) : ListDelegationAdapter<MutableList<ListItem>>() {

    private var currentScreenKey: String? = null

    init {
        items = mutableListOf()
        delegatesManager.run {
            addDelegate(BottomTabDelegate(listener))
        }
    }

    fun bindItems(tabs: List<MainActivity.Tab>) {
        this.items.clear()
        this.items.addAll(tabs.map { BottomTabListItem(it) })
        notifyDataSetChanged()
        currentScreenKey?.let { setSelected(it) }
    }

    fun setSelected(screenKey: String) {
        currentScreenKey = screenKey
        items.forEachIndexed { index, item ->
            val listItem = (item as BottomTabListItem)
            val lastSelected = listItem.selected
            listItem.selected = listItem.item.screen.screenKey == screenKey
            if (lastSelected != listItem.selected) {
                notifyItemChanged(index)
            }
        }
    }

    interface Listener : BottomTabDelegate.Listener
}