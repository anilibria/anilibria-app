package ru.radiationx.anilibria.ui.adapters.release.detail

import android.text.Html
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_release_blocked.*
import ru.radiationx.anilibria.R
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
        override val containerView: View
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(state: ReleaseBlockedInfoState) {
            item_title.text = Html.fromHtml(state.title)
        }
    }
}