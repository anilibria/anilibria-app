package ru.radiationx.anilibria.ui.adapters.release.detail

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_release_torrent.*
import ru.radiationx.anilibria.R
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
        override val containerView: View,
        private val clickListener: (ReleaseTorrentItemState) -> Unit
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(state: ReleaseTorrentItemState) {
            itemTorrentTitle.text = state.title
            itemTorrentDesc.text = state.subtitle
            itemTorrentSize.text = state.size
            itemTorrentSeeders.text = state.seeders
            itemTorrentLeechers.text = state.leechers
            itemTorrentDate.text = state.date?.relativeDate(itemTorrentDate.context)
            itemTorrentDate.isVisible = state.date != null
            itemTorrentPreffer.isVisible = state.isPrefer

            containerView.setOnClickListener { clickListener.invoke(state) }
        }
    }

}