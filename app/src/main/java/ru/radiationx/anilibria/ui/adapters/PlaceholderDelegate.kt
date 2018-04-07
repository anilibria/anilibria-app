package ru.radiationx.anilibria.ui.adapters

import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import kotlinx.android.synthetic.main.item_placeholder.view.*
import ru.radiationx.anilibria.R

class PlaceholderDelegate() : AdapterDelegate<MutableList<ListItem>>() {

    override fun isForViewType(items: MutableList<ListItem>, position: Int): Boolean = items[position] is PlaceholderListItem

    override fun onBindViewHolder(items: MutableList<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val item = items[position] as PlaceholderListItem
        (holder as ViewHolder).bind(item.icRes, item.titleRes, item.descRes)
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_placeholder, parent, false))

    private inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(icRes: Int, titleRes: Int, descRes: Int) {
            view.run {
                item_placeholder_icon.setImageDrawable(ContextCompat.getDrawable(context, icRes))
                item_placeholder_icon.drawable?.setColorFilter(ContextCompat.getColor(context, R.color.base_icon), PorterDuff.Mode.SRC_ATOP)
                item_placeholder_title.setText(titleRes)
                item_placeholder_desc.setText(descRes)
            }
        }
    }
}
