package ru.radiationx.anilibria.ui.common.adapters

import androidx.recyclerview.widget.DiffUtil
import ru.radiationx.anilibria.ui.adapters.ListItem

open class ListItemAdapter : OptimizeAdapter<ListItem>(ListItemDiffCallback)

object ListItemDiffCallback : DiffUtil.ItemCallback<ListItem>() {

    override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        return oldItem.getItemId() == newItem.getItemId()
    }

    override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        return oldItem.getItemHash() == newItem.getItemHash()
    }

    override fun getChangePayload(oldItem: ListItem, newItem: ListItem): Any? {
        return newItem.getPayloadBy(oldItem)
    }
}