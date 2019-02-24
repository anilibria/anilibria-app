package ru.radiationx.anilibria.ui.adapters.youtube

import android.os.Build
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.synthetic.main.item_youtube.view.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.youtube.YoutubeItem
import ru.radiationx.anilibria.ui.adapters.BaseItemListener
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.YoutubeListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeDelegate
import ru.radiationx.anilibria.ui.fragments.release.details.ReleaseFragment

/**
 * Created by radiationx on 13.01.18.
 */
class YoutubeDelegate(
        private val itemListener: Listener
) : AppAdapterDelegate<YoutubeListItem, ListItem, YoutubeDelegate.ViewHolder>(
        R.layout.item_youtube,
        { it is YoutubeListItem },
        { ViewHolder(it, itemListener) }
), OptimizeDelegate {

    override fun getPoolSize(): Int = 10

    override fun bindData(item: YoutubeListItem, holder: ViewHolder) = holder.bind(item.item)

    class ViewHolder(
            val view: View,
            private val itemListener: Listener
    ) : RecyclerView.ViewHolder(view) {

        private lateinit var currentItem: YoutubeItem

        init {
            itemView.run {
                setOnClickListener {
                    itemListener.onItemClick(currentItem, layoutPosition)
                }
            }
        }

        fun bind(item: YoutubeItem) {
            currentItem = item
            view.run {
                item_title.text = item.title

                item_views_count.text = item.views.toString()
                item_comments_count.text = item.comments.toString()

                ImageLoader.getInstance().displayImage(item.image, item_image)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    item_image.transitionName = "${ReleaseFragment.TRANSACTION}_${item.id}"
                }
            }
        }
    }

    interface Listener : BaseItemListener<YoutubeItem>
}