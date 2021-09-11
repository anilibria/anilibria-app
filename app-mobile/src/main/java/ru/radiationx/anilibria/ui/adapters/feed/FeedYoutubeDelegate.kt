package ru.radiationx.anilibria.ui.adapters.feed

import android.view.View
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.synthetic.main.item_feed_youtube.view.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.model.YoutubeItemState
import ru.radiationx.anilibria.ui.adapters.FeedListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.YoutubeListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeDelegate
import ru.radiationx.data.entity.app.youtube.YoutubeItem

/**
 * Created by radiationx on 13.01.18.
 */
class FeedYoutubeDelegate(
    private val clickListener: (YoutubeItemState, View) -> Unit
) : AppAdapterDelegate<FeedListItem, ListItem, FeedYoutubeDelegate.ViewHolder>(
    R.layout.item_feed_youtube,
    { (it as? FeedListItem)?.item?.youtube != null },
    { ViewHolder(it, clickListener) }
), OptimizeDelegate {

    override fun getPoolSize(): Int = 5

    override fun bindData(item: FeedListItem, holder: ViewHolder) = holder.bind(item)

    class ViewHolder(
        val view: View,
        private val clickListener: (YoutubeItemState, View) -> Unit
    ) : RecyclerView.ViewHolder(view) {

        fun bind(item: FeedListItem) {
            val state = requireNotNull(item.item.youtube)
            view.apply {
                item_title.text = state.title

                item_views_count.text = state.views.toString()
                item_comments_count.text = state.comments.toString()

                ImageLoader.getInstance().displayImage(state.image, item_image)
                ViewCompat.setTransitionName(item_image, "${item.javaClass.simpleName}_${state.id}")
            }
            itemView.run {
                setOnClickListener {
                    clickListener.invoke(state, item_image)
                }
            }
        }
    }
}