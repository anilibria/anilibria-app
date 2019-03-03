package ru.radiationx.anilibria.ui.adapters.feed

import android.os.Build
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.View
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_feed_release.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.extension.visible
import ru.radiationx.anilibria.ui.adapters.BaseItemListener
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeDelegate
import ru.radiationx.anilibria.ui.fragments.release.details.ReleaseFragment

/**
 * Created by radiationx on 13.01.18.
 */
class FeedReleaseDelegate(
        private val itemListener: Listener
) : AppAdapterDelegate<ReleaseListItem, ListItem, FeedReleaseDelegate.ViewHolder>(
        R.layout.item_feed_release,
        { it is ReleaseListItem },
        { ViewHolder(it, itemListener) }
), OptimizeDelegate {

    override fun getPoolSize(): Int = 10

    override fun bindData(item: ReleaseListItem, holder: ViewHolder) = holder.bind(item.item)

    class ViewHolder(
            override val containerView: View,
            private val itemListener: Listener
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        private lateinit var currentItem: ReleaseItem

        init {
            containerView.setOnClickListener {
                itemListener.onItemClick(layoutPosition, item_image)
                itemListener.onItemClick(currentItem, layoutPosition)
            }
            containerView.setOnLongClickListener {
                itemListener.onItemLongClick(currentItem)
            }
        }

        fun bind(item: ReleaseItem) {
            currentItem = item
            if (item.series == null) {
                item_title.text = item.title
            } else {
                item_title.text = String.format("%s (%s)", item.title, item.series)
            }
            item_desc.text = Html.fromHtml(item.description)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //item_image.transitionName = ReleaseFragment.TRANSACTION + "_" + position
                item_image.transitionName = "${ReleaseFragment.TRANSACTION}_${item.id}"
            }
            item_new_indicator.visible(item.isNew)
            ImageLoader.getInstance().displayImage(item.poster, item_image)
        }
    }

    interface Listener : BaseItemListener<ReleaseItem> {
        fun onItemClick(position: Int, view: View)
    }
}