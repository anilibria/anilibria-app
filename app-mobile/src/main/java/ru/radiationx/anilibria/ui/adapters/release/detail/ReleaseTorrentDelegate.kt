package ru.radiationx.anilibria.ui.adapters.release.detail

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import dev.androidbroadcast.vbpd.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemReleaseTorrentBinding
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseTorrentListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeDelegate
import ru.radiationx.anilibria.ui.fragments.release.details.ReleaseTorrentItemState
import ru.radiationx.anilibria.utils.dimensions.Side
import ru.radiationx.anilibria.utils.dimensions.dimensionsApplier
import ru.radiationx.data.common.TorrentId
import ru.radiationx.shared.ktx.android.relativeDate

/**
 * Created by radiationx on 13.01.18.
 */
class ReleaseTorrentDelegate(
    private val clickListener: (TorrentId) -> Unit,
    private val cancelClickListener: (TorrentId) -> Unit,
) : AppAdapterDelegate<ReleaseTorrentListItem, ListItem, ReleaseTorrentDelegate.ViewHolder>(
    R.layout.item_release_torrent,
    { it is ReleaseTorrentListItem },
    { ViewHolder(it, clickListener, cancelClickListener) }
), OptimizeDelegate {

    override fun getPoolSize(): Int = 20

    override fun bindData(item: ReleaseTorrentListItem, holder: ViewHolder) =
        holder.bind(item.state)

    class ViewHolder(
        itemView: View,
        private val clickListener: (TorrentId) -> Unit,
        private val cancelClickListener: (TorrentId) -> Unit,
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemReleaseTorrentBinding>()

        private val dimensionsApplier by dimensionsApplier()

        fun bind(state: ReleaseTorrentItemState) {
            dimensionsApplier.applyPaddings(Side.Left, Side.Right)
            binding.itemTorrentTitle.text = state.title
            binding.itemTorrentDesc.text = state.subtitle
            binding.itemTorrentSize.text = state.size
            binding.itemTorrentSeeders.text = state.seeders
            binding.itemTorrentLeechers.text = state.leechers
            binding.itemTorrentDate.text = state.date?.relativeDate(binding.itemTorrentDate.context)
            binding.itemTorrentDate.isVisible = state.date != null
            binding.itemTorrentPreffer.isVisible = state.isPrefer

            binding.itemTorrentProgress.bindProgress(state.progress)
            binding.itemTorrentProgress.actionClickListener = {
                clickListener.invoke(state.id)
            }
            binding.itemTorrentProgress.cancelClickListener = {
                cancelClickListener.invoke(state.id)
            }

            binding.root.setOnClickListener { clickListener.invoke(state.id) }
        }
    }

}