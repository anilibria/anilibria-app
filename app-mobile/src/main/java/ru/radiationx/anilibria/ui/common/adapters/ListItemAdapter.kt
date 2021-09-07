package ru.radiationx.anilibria.ui.common.adapters

import androidx.recyclerview.widget.DiffUtil
import ru.radiationx.anilibria.ui.adapters.ListItem

open class ListItemAdapter : OptimizeAdapter<ListItem>(ListItemDiffCallback) {


}

object ListItemDiffCallback : DiffUtil.ItemCallback<ListItem>() {
    override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        TODO("Not yet implemented")
    }

    override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        TODO("Not yet implemented")
    }

}