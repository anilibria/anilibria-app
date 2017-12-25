package ru.radiationx.anilibria.ui.fragments.articles

import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

import com.nostra13.universalimageloader.core.ImageLoader

import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.data.api.models.article.ArticleItem
import ru.radiationx.anilibria.ui.adapters.BaseAdapter
import ru.radiationx.anilibria.ui.adapters.BaseViewHolder
import ru.radiationx.anilibria.ui.fragments.release.ReleaseFragment
import ru.radiationx.anilibria.ui.widgets.AspectRatioImageView

/* Created by radiationx on 31.10.17. */

open class ArticlesAdapter : BaseAdapter<ArticleItem, BaseViewHolder<*>>() {
    private var listener: ItemListener? = null
    var endless: Boolean = false
        set(enable) {
            field = enable
            notifyDataSetChanged()
        }

    fun setListener(listener: ItemListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*>? {
        when (viewType) {
            ARTICLE_LAYOUT -> return ArticleItemHolder(inflateLayout(parent, R.layout.item_article))
            LOAD_MORE_LAYOUT -> return LoadMoreHolder(inflateLayout(parent, R.layout.item_load_more))
        }
        return null
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val itemType = getItemViewType(position)
        if (itemType == ARTICLE_LAYOUT) {
            (holder as ArticleItemHolder).bind(getItem(position), position)
        } else if (itemType == LOAD_MORE_LAYOUT) {
            holder.bind(position)
        }
    }

    override fun getItemCount(): Int {
        var count = super.getItemCount()
        if (endless && count > 0) {
            count++
        }
        return count
    }

    override fun getItemViewType(position: Int): Int {
        return if (endless && position == itemCount - 1) {
            LOAD_MORE_LAYOUT
        } else ARTICLE_LAYOUT
    }

    fun insertMore(list: List<ArticleItem>) {
        val prevItems = itemCount
        this.items.addAll(list)
        Log.d("SUKA", "insertMore $prevItems : $itemCount")
        notifyItemRangeInserted(prevItems, itemCount)
    }

    internal inner class ArticleItemHolder(itemView: View) : BaseViewHolder<ArticleItem>(itemView) {
        var image: AspectRatioImageView = itemView.findViewById(R.id.item_image)
        var title: TextView = itemView.findViewById(R.id.item_title)
        var content: TextView = itemView.findViewById(R.id.item_content)
        var nick: TextView = itemView.findViewById(R.id.item_nick)
        var viewsCount: TextView = itemView.findViewById(R.id.item_views_count)
        var commentsCount: TextView = itemView.findViewById(R.id.item_comments_count)

        init {
            itemView.setOnClickListener {
                listener?.onItemClick(layoutPosition, image)
                listener?.onItemClick(getItem(layoutPosition), layoutPosition)
            }
        }

        override fun bind(item: ArticleItem, position: Int) {
            title.text = item.title

            content.visibility = View.GONE
            /*if (item.content.isBlank()) {
                content.visibility = View.GONE
            } else {
                content.visibility = View.VISIBLE
                content.text = item.content
            }*/

            nick.text = item.userNick

            viewsCount.text = item.viewsCount.toString()
            commentsCount.text = item.commentsCount.toString()

            image.setAspectRatio(item.imageHeight.div(item.imageWidth.toFloat()))
            ImageLoader.getInstance().displayImage(item.imageUrl, image)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                image.transitionName = ReleaseFragment.TRANSACTION + "_" + position
            }
        }
    }

    private inner class LoadMoreHolder internal constructor(itemView: View) : BaseViewHolder<ArticleItem>(itemView) {
        private val container: LinearLayout = itemView.findViewById(R.id.nl_lm_container)
        private val btn: Button = itemView.findViewById(R.id.nl_lm_btn)

        init {
            btn.visibility = View.GONE
            container.visibility = View.VISIBLE
        }

        override fun bind(position: Int) {
            Log.d("SUKA", "BIND LOAD_MORE")
            listener?.onLoadMore()
        }
    }

    interface ItemListener : BaseAdapter.OnItemClickListener<ArticleItem> {
        fun onItemClick(position: Int, view: View)
        fun onLoadMore()
    }

    companion object {
        private val ARTICLE_LAYOUT = 1
        private val LOAD_MORE_LAYOUT = 2
    }
}
