package ru.radiationx.anilibria.ui.adapters.feed

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_feed_random_btn.*
import ru.radiationx.anilibria.R
import ru.radiationx.shared.ktx.android.visible
import ru.radiationx.anilibria.ui.adapters.FeedRandomBtnListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate

/**
 * Created by radiationx on 13.01.18.
 */
class FeedRandomBtnDelegate(
        private val clickListener: () -> Unit
) : AppAdapterDelegate<FeedRandomBtnListItem, ListItem, FeedRandomBtnDelegate.ViewHolder>(
        R.layout.item_feed_random_btn,
        { it is FeedRandomBtnListItem },
        { ViewHolder(it, clickListener) }
) {

    override fun bindData(item: FeedRandomBtnListItem, holder: ViewHolder) =
            holder.bind()

    class ViewHolder(
            override val containerView: View,
            private val clickListener: () -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.ViewHolder(containerView), LayoutContainer {

        init {
            item_random_btn.setOnClickListener {
                clickListener.invoke()
            }
        }

        fun bind() {}
    }
}