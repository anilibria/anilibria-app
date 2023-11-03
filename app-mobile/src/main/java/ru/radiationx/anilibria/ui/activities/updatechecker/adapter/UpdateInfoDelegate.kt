package ru.radiationx.anilibria.ui.activities.updatechecker.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemUpdateInfoBinding
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate

class UpdateInfoDelegate :
    AppAdapterDelegate<UpdateInfoListItem, ListItem, UpdateInfoDelegate.ViewHolder>(
        R.layout.item_update_info,
        { it is UpdateInfoListItem },
        { ViewHolder(it) }
    ) {

    override fun bindData(item: UpdateInfoListItem, holder: ViewHolder) =
        holder.bind(item)

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemUpdateInfoBinding>()

        fun bind(item: UpdateInfoListItem) {
            val desc = buildString {
                val lastIndex = item.items.lastIndex
                item.items.forEachIndexed { index, s ->
                    append("— ")
                    if (lastIndex == index) {
                        append(s)
                    } else {
                        appendLine(s)
                    }
                }
            }
            binding.itemUpdaterInfoTitle.text = item.title
            binding.itemUpdaterInfoDesc.text = desc
        }
    }
}