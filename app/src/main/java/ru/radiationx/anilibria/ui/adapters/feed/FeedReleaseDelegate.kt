package ru.radiationx.anilibria.ui.adapters.feed

import android.os.Build
import android.support.v4.view.ViewCompat
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.View
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_feed_release.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.entity.app.youtube.YoutubeItem
import ru.radiationx.anilibria.extension.visible
import ru.radiationx.anilibria.ui.adapters.*
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeDelegate
import ru.radiationx.anilibria.ui.fragments.release.details.ReleaseFragment

/**
 * Created by radiationx on 13.01.18.
 */
class FeedReleaseDelegate(
        private val clickListener: (ReleaseItem, View) -> Unit,
        private val longClickListener: (ReleaseItem, View) -> Unit
) : AppAdapterDelegate<FeedListItem, ListItem, FeedReleaseDelegate.ViewHolder>(
        R.layout.item_feed_release,
        { (it as? FeedListItem)?.item?.release != null },
        { ViewHolder(it, clickListener, longClickListener) }
), OptimizeDelegate {

    override fun getPoolSize(): Int = 5

    override fun bindData(item: FeedListItem, holder: ViewHolder) = holder.bind(item.item.release!!)


    override fun applyPayloads(item: FeedListItem, payloads: MutableList<Any>, holder: ViewHolder) {
        payloads.filterIsInstance<FeedUpdateDataPayload>().forEach {
            holder.updateItem(item.item.release!!)
        }
    }

    class ViewHolder(
            override val containerView: View,
            private val clickListener: (ReleaseItem, View) -> Unit,
            private val longClickListener: (ReleaseItem, View) -> Unit
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        private lateinit var currentItem: ReleaseItem

        init {
            containerView.setOnClickListener {
                clickListener.invoke(currentItem, item_image)
            }
            containerView.setOnLongClickListener {
                longClickListener.invoke(currentItem, item_image)
                return@setOnLongClickListener false
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
            ViewCompat.setTransitionName(item_image, "${item.javaClass.simpleName}_${item.id}")
            item_new_indicator.visible(item.isNew)
            ImageLoader.getInstance().displayImage(item.poster, item_image)
        }

        fun updateItem(item: ReleaseItem) {
            currentItem = item
        }
    }
}