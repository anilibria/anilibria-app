package ru.radiationx.anilibria.ui.activities.updatechecker.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import dev.androidbroadcast.vbpd.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemUpdateLinkBinding
import ru.radiationx.anilibria.ui.activities.updatechecker.UpdateLinkState
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.data.entity.domain.updater.UpdateData

class UpdateLinkDelegate(
    private val actionClickListener: (UpdateData.UpdateLink) -> Unit,
    private val cancelClickListener: (UpdateData.UpdateLink) -> Unit,
) : AppAdapterDelegate<UpdateLinkListItem, ListItem, UpdateLinkDelegate.ViewHolder>(
    R.layout.item_update_link,
    { it is UpdateLinkListItem },
    { ViewHolder(it, actionClickListener, cancelClickListener) }
) {

    override fun bindData(item: UpdateLinkListItem, holder: ViewHolder) =
        holder.bind(item.data)

    class ViewHolder(
        itemView: View,
        private val actionClickListener: (UpdateData.UpdateLink) -> Unit,
        private val cancelClickListener: (UpdateData.UpdateLink) -> Unit,
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemUpdateLinkBinding>()

        fun bind(data: UpdateLinkState) {
            val actionIcon = when (data.link.type) {
                UpdateData.LinkType.FILE -> R.drawable.ic_file_download
                UpdateData.LinkType.SITE -> R.drawable.ic_link
            }

            binding.itemUpdaterName.text = data.link.name
            binding.itemUpdateProgress.bindProgress(data.progress)
            binding.itemUpdateProgress.setActionIconRes(actionIcon)

            binding.itemUpdateProgress.actionClickListener = {
                actionClickListener.invoke(data.link)
            }
            binding.itemUpdateProgress.cancelClickListener = {
                cancelClickListener.invoke(data.link)
            }
            binding.root.setOnClickListener {
                actionClickListener.invoke(data.link)
            }
        }
    }
}