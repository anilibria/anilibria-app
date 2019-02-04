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
import ru.radiationx.anilibria.entity.app.article.ArticleItem
import ru.radiationx.anilibria.entity.app.youtube.YoutubeItem
import ru.radiationx.anilibria.ui.adapters.ArticleListItem
import ru.radiationx.anilibria.ui.adapters.BaseItemListener
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.YoutubeListItem
import ru.radiationx.anilibria.ui.common.adapters.OptimizeDelegate
import ru.radiationx.anilibria.ui.fragments.release.details.ReleaseFragment

/**
 * Created by radiationx on 13.01.18.
 */
class YoutubeDelegate(private val itemListener: Listener) : OptimizeDelegate<MutableList<ListItem>>() {

    override fun getPoolSize(): Int = 10

    override fun isForViewType(items: MutableList<ListItem>, position: Int): Boolean = items[position] is YoutubeListItem

    override fun onBindViewHolder(items: MutableList<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val item = items[position] as YoutubeListItem
        (holder as ViewHolder).bind(item.item)
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_youtube, parent, false)
    )

    private inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

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