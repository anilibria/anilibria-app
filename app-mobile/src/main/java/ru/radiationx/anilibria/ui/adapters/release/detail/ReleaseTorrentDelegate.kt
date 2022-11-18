package ru.radiationx.anilibria.ui.adapters.release.detail

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemReleaseTorrentBinding
import ru.radiationx.anilibria.presentation.release.details.ReleaseTorrentItemState
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseTorrentListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeDelegate
import ru.radiationx.shared.ktx.android.relativeDate

/**
 * Created by radiationx on 13.01.18.
 */
class ReleaseTorrentDelegate(
    private val clickListener: (ReleaseTorrentItemState) -> Unit
) : AppAdapterDelegate<ReleaseTorrentListItem, ListItem, ReleaseTorrentDelegate.ViewHolder>(
    R.layout.item_release_torrent,
    { it is ReleaseTorrentListItem },
    { ViewHolder(it, clickListener) }
), OptimizeDelegate {

    override fun getPoolSize(): Int = 20

    override fun bindData(item: ReleaseTorrentListItem, holder: ViewHolder) =
        holder.bind(item.state)

    class ViewHolder(
        itemView: View,
        private val clickListener: (ReleaseTorrentItemState) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemReleaseTorrentBinding>()

        fun bind(state: ReleaseTorrentItemState) {
            binding.itemTorrentTitle.text = state.title
            binding.itemTorrentDesc.text = state.subtitle
            binding.itemTorrentSize.text = state.size
            binding.itemTorrentSeeders.text = state.seeders
            binding.itemTorrentLeechers.text = state.leechers
            binding.itemTorrentDate.text = state.date?.relativeDate(binding.itemTorrentDate.context)
            binding.itemTorrentDate.isVisible = state.date != null
            binding.itemTorrentPreffer.isVisible = state.isPrefer

            binding.root.setOnClickListener { clickListener.invoke(state) }
        }
    }

}