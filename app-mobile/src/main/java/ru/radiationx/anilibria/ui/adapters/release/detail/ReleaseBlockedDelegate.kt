package ru.radiationx.anilibria.ui.adapters.release.detail

import android.text.Html
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemReleaseBlockedBinding
import ru.radiationx.anilibria.presentation.release.details.ReleaseBlockedInfoState
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseBlockedListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate

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

        fun bind(state: ReleaseBlockedInfoState) {
            binding.itemTitle.text = Html.fromHtml(state.title)
        }
    }
}