package ru.radiationx.anilibria.ui.adapters.search

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_fast_search.*
import ru.radiationx.anilibria.R
import ru.radiationx.data.entity.app.search.SearchItem
import ru.radiationx.shared.ktx.android.setCompatDrawable
import ru.radiationx.shared.ktx.android.setTintColorAttr
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
    ) : androidx.recyclerview.widget.RecyclerView.ViewHolder(containerView), LayoutContainer {

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
            item_image.setTintColorAttr(R.attr.colorOnSurface)
            item_image.background = null
            item_title.text = item.title
        }

    }
}