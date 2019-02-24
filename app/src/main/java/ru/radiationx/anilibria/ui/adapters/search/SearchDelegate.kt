package ru.radiationx.anilibria.ui.adapters.search

import android.content.res.ColorStateList
import android.support.v4.content.ContextCompat
import android.support.v4.widget.ImageViewCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_fast_search.*
import kotlinx.android.synthetic.main.item_fast_search.view.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.search.SearchItem
import ru.radiationx.anilibria.extension.getColorFromAttr
import ru.radiationx.anilibria.extension.setCompatDrawable
import ru.radiationx.anilibria.extension.setTintColorAttr
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.SearchListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeDelegate

/**
 * Created by radiationx on 13.01.18.
 */
class SearchDelegate(
        private val clickListener: (SearchItem) -> Unit
) : AppAdapterDelegate<SearchListItem, ListItem, SearchDelegate.ViewHolder>(
        R.layout.item_fast_search,
        { it is SearchListItem },
        { ViewHolder(it, clickListener) }
), OptimizeDelegate {

    override fun getPoolSize(): Int = 15

    override fun bindData(item: SearchListItem, holder: ViewHolder) = holder.bind(item.item)

    class ViewHolder(
            override val containerView: View,
            private val clickListener: (SearchItem) -> Unit
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        private lateinit var currentItem: SearchItem

        init {
            containerView.setOnClickListener {
                clickListener.invoke(currentItem)
            }
            item_image.scaleType = ImageView.ScaleType.CENTER
        }

        fun bind(item: SearchItem) {
            currentItem = item
            item_image.setCompatDrawable(item.icRes)
            item_image.setTintColorAttr(R.attr.base_icon)
            item_title.text = item.title
        }

    }
}