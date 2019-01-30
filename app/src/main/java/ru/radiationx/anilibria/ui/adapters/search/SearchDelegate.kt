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
import kotlinx.android.synthetic.main.item_fast_search.view.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.search.SearchItem
import ru.radiationx.anilibria.extension.getColorFromAttr
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.SearchListItem

/**
 * Created by radiationx on 13.01.18.
 */
class SearchDelegate(
        private val clickListener: (SearchItem) -> Unit
) : AdapterDelegate<MutableList<ListItem>>() {

    override fun isForViewType(items: MutableList<ListItem>, position: Int): Boolean = items[position] is SearchListItem

    override fun onBindViewHolder(items: MutableList<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        (items[position] as SearchListItem).also {
            (holder as ViewHolder).bind(it.item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_fast_search, parent, false)
    )

    private inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        private lateinit var currentItem: SearchItem

        init {
            view.setOnClickListener {
                clickListener.invoke(currentItem)
            }
            view.item_image.scaleType = ImageView.ScaleType.CENTER
        }

        fun bind(item: SearchItem) {
            currentItem = item
            view.apply {
                item_image.setImageDrawable(ContextCompat.getDrawable(context, item.icRes))
                ImageViewCompat.setImageTintList(item_image, ColorStateList.valueOf(context.getColorFromAttr(R.attr.base_icon)))
                item_title.text = item.title
            }
        }

    }
}