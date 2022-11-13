package ru.radiationx.anilibria.ui.adapters.youtube

import android.view.View
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_feed_youtube.view.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.model.YoutubeItemState
import ru.radiationx.anilibria.ui.adapters.BaseItemListener
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.YoutubeListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeDelegate
import ru.radiationx.shared_app.imageloader.showImageUrl

/**
 * Created by radiationx on 13.01.18.
 */
class YoutubeDelegate(
    private val itemListener: Listener
) : AppAdapterDelegate<YoutubeListItem, ListItem, YoutubeDelegate.ViewHolder>(
    R.layout.item_feed_youtube,
    { it is YoutubeListItem },
    { ViewHolder(it, itemListener) }
), OptimizeDelegate {

    override fun getPoolSize(): Int = 10

    override fun bindData(item: YoutubeListItem, holder: ViewHolder) = holder.bind(item)

    class ViewHolder(
        private val view: View,
        private val itemListener: Listener
    ) : RecyclerView.ViewHolder(view) {

        fun bind(item: YoutubeListItem) {
            view.run {
                item_title.text = item.state.title

                item_views_count.text = item.state.views
                item_comments_count.text = item.state.comments

                item_image.showImageUrl(item.state.image)
                ViewCompat.setTransitionName(item_image, "${item.javaClass.simpleName}_${item.state.id}")
                setOnClickListener {
                    itemListener.onItemClick(item.state, layoutPosition)
                }
            }
        }
    }

    interface Listener : BaseItemListener<YoutubeItemState>
}