package ru.radiationx.anilibria.ui.adapters.release.detail

import android.view.View
import androidx.core.text.parseAsHtml
import androidx.recyclerview.widget.RecyclerView
import dev.androidbroadcast.vbpd.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemReleaseBlockedBinding
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseBlockedListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.fragments.release.details.ReleaseBlockedInfoState
import ru.radiationx.anilibria.utils.dimensions.Side
import ru.radiationx.anilibria.utils.dimensions.dimensionsApplier

/**
 * Created by radiationx on 21.01.18.
 */
class ReleaseBlockedDelegate :
    AppAdapterDelegate<ReleaseBlockedListItem, ListItem, ReleaseBlockedDelegate.ViewHolder>(
        R.layout.item_release_blocked,
        { it is ReleaseBlockedListItem },
        { ViewHolder(it) }
    ) {

    override fun bindData(item: ReleaseBlockedListItem, holder: ViewHolder) =
        holder.bind(item.state)

    class ViewHolder(
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemReleaseBlockedBinding>()

        private val dimensionsApplier by dimensionsApplier()

        fun bind(state: ReleaseBlockedInfoState) {
            dimensionsApplier.applyPaddings(Side.Left, Side.Right)
            binding.itemTitle.text = state.title.parseAsHtml()
        }
    }
}