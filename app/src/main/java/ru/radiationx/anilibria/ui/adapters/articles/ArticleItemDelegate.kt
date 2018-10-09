package ru.radiationx.anilibria.ui.adapters.articles

import android.os.Build
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.synthetic.main.item_article.view.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.article.ArticleItem
import ru.radiationx.anilibria.ui.adapters.ArticleListItem
import ru.radiationx.anilibria.ui.adapters.BaseItemListener
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.fragments.release.details.ReleaseFragment

/**
 * Created by radiationx on 13.01.18.
 */
class ArticleItemDelegate(private val itemListener: Listener) : AdapterDelegate<MutableList<ListItem>>() {
    override fun isForViewType(items: MutableList<ListItem>, position: Int): Boolean = items[position] is ArticleListItem

    override fun onBindViewHolder(items: MutableList<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val item = items[position] as ArticleListItem
        (holder as ViewHolder).bind(item.item, position, item.transitionAppendix)
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_article, parent, false)
    )

    private inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        private lateinit var currentItem: ArticleItem

        init {
            itemView.run {
                setOnClickListener {
                    itemListener.onItemClick(layoutPosition, item_image)
                    itemListener.onItemClick(currentItem, layoutPosition)
                }
            }
        }

        fun bind(item: ArticleItem, position: Int, transitionAppendix: String) {
            currentItem = item
            view.run {
                item_title.text = item.title

                item_content.visibility = View.GONE
                /*if (item.content.isBlank()) {
                    content.visibility = View.GONE
                } else {
                    content.visibility = View.VISIBLE
                    content.text = item.content
                }*/

                item_nick.text = item.userNick

                item_views_count.text = item.viewsCount.toString()
                item_comments_count.text = item.commentsCount.toString()

                item_image.setAspectRatio(item.imageHeight.div(item.imageWidth.toFloat()))
                ImageLoader.getInstance().displayImage(item.imageUrl, item_image)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    item_image.transitionName = "${ReleaseFragment.TRANSACTION}_${item.id}_${position}_$transitionAppendix"
                }
            }
        }
    }

    interface Listener : BaseItemListener<ArticleItem> {
        fun onItemClick(position: Int, view: View)
    }
}