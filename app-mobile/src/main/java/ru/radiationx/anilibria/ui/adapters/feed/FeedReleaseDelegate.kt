package ru.radiationx.anilibria.ui.adapters.feed

import android.text.Html
import android.view.View
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_feed_release.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.anilibria.ui.adapters.FeedListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeDelegate
import ru.radiationx.shared.ktx.android.visible
import ru.radiationx.shared_app.imageloader.showImageUrl

/**
 * Created by radiationx on 13.01.18.
 */
class FeedReleaseDelegate(
    private val clickListener: (ReleaseItemState, View) -> Unit,
    private val longClickListener: (ReleaseItemState, View) -> Unit
) : AppAdapterDelegate<FeedListItem, ListItem, FeedReleaseDelegate.ViewHolder>(
    R.layout.item_feed_release,
    { (it as? FeedListItem)?.item?.release != null },
    { ViewHolder(it, clickListener, longClickListener) }
), OptimizeDelegate {

    override fun getPoolSize(): Int = 5

    override fun bindData(item: FeedListItem, holder: ViewHolder) = holder.bind(item)

    class ViewHolder(
        override val containerView: View,
        private val clickListener: (ReleaseItemState, View) -> Unit,
        private val longClickListener: (ReleaseItemState, View) -> Unit
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(item: FeedListItem) {
            val state = requireNotNull(item.item.release)

            item_title.text = state.title
            item_desc.text = Html.fromHtml(state.description)
            ViewCompat.setTransitionName(item_image, "${item.javaClass.simpleName}_${state.id}")
            item_new_indicator.visible(state.isNew)
            item_image.showImageUrl(state.posterUrl)


            containerView.setOnClickListener {
                clickListener.invoke(state, item_image)
            }
            containerView.setOnLongClickListener {
                longClickListener.invoke(state, item_image)
                return@setOnLongClickListener false
            }
        }
    }
}