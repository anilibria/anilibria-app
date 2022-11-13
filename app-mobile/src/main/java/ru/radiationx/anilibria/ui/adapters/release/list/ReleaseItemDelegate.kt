package ru.radiationx.anilibria.ui.adapters.release.list

import android.text.Html
import android.view.View
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_feed_release.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.anilibria.ui.adapters.BaseItemListener
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeDelegate
import ru.radiationx.shared.ktx.android.visible
import ru.radiationx.shared_app.imageloader.showImageUrl

/**
 * Created by radiationx on 13.01.18.
 */
class ReleaseItemDelegate(
    private val itemListener: Listener
) : AppAdapterDelegate<ReleaseListItem, ListItem, ReleaseItemDelegate.ViewHolder>(
    R.layout.item_feed_release,
    { it is ReleaseListItem },
    { ViewHolder(it, itemListener) }
), OptimizeDelegate {

    override fun getPoolSize(): Int = 10

    override fun bindData(item: ReleaseListItem, holder: ViewHolder) = holder.bind(item)

    class ViewHolder(
        override val containerView: View,
        private val itemListener: Listener
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {


        fun bind(item: ReleaseListItem) {
            val releaseItem = item.item
            item_title.text = releaseItem.title

            item_desc.text = Html.fromHtml(releaseItem.description)
            ViewCompat.setTransitionName(item_image, "${item.javaClass.simpleName}_${releaseItem.id}")
            item_new_indicator.visible(releaseItem.isNew)
            item_image.showImageUrl(releaseItem.posterUrl)

            containerView.setOnClickListener {
                itemListener.onItemClick(layoutPosition, item_image)
                itemListener.onItemClick(releaseItem, layoutPosition)
            }
            containerView.setOnLongClickListener {
                itemListener.onItemLongClick(releaseItem)
            }
        }
    }

    interface Listener : BaseItemListener<ReleaseItemState> {
        fun onItemClick(position: Int, view: View)
    }
}