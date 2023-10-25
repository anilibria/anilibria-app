package ru.radiationx.anilibria.ui.activities.updatechecker.adapter

import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemUpdateLinkBinding
import ru.radiationx.anilibria.ui.activities.updatechecker.UpdateLinkState
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.data.entity.domain.updater.UpdateData
import ru.radiationx.shared.ktx.android.setCompatDrawable

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

        private var progressJob: Job? = null

        fun bind(data: UpdateLinkState) {
            progressJob?.cancel()
            progressJob = null
            binding.itemUpdaterName.text = data.link.name

            binding.itemUpdaterCancel.isVisible = data.progress != null
            binding.itemUpdaterAction.isVisible = data.progress == null
            binding.itemUpdaterProgress.isVisible = data.progress != null

            val actionIcon = when (data.link.type) {
                UpdateData.LinkType.FILE -> R.drawable.ic_file_download
                UpdateData.LinkType.SITE -> R.drawable.ic_link
            }
            binding.itemUpdaterAction.setCompatDrawable(actionIcon)

            data.progress?.also { bindProgress(it) }

            binding.root.setOnClickListener {
                actionClickListener.invoke(data.link)
            }
            binding.itemUpdaterAction.setOnClickListener {
                actionClickListener.invoke(data.link)
            }
            binding.itemUpdaterCancel.setOnClickListener {
                cancelClickListener.invoke(data.link)
            }
        }

        private fun bindProgress(progressFlow: StateFlow<Int>) {
            val lifecycleOwner = binding.root.findViewTreeLifecycleOwner() ?: return
            progressJob = progressFlow.onEach {
                binding.itemUpdaterProgress.progress = it
                binding.itemUpdaterProgress.isIndeterminate = it <= 0
            }.launchIn(lifecycleOwner.lifecycleScope)
        }
    }
}