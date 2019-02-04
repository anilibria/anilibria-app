package ru.radiationx.anilibria.ui.adapters.other

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.adapters.DividerShadowListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.OptimizeDelegate

class DividerShadowItemDelegate : OptimizeDelegate<MutableList<ListItem>>() {

    override fun isForViewType(items: MutableList<ListItem>, position: Int): Boolean
            = items[position] is DividerShadowListItem

    override fun onBindViewHolder(items: MutableList<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder
            = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_other_divider_shadow, parent, false))

    private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
