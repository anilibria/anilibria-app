package ru.radiationx.anilibria.ui.adapters.search

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import kotlinx.android.synthetic.main.item_genre.view.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.release.GenreItem
import ru.radiationx.anilibria.ui.adapters.BaseItemListener
import ru.radiationx.anilibria.ui.adapters.GenreListItem
import ru.radiationx.anilibria.ui.adapters.ListItem

/**
 * Created by radiationx on 13.01.18.
 */
class GenreItemDelegate(private val listener: BaseItemListener<GenreItem>) : AdapterDelegate<MutableList<ListItem>>() {

    private var checkedGenre: String = ""

    override fun isForViewType(items: MutableList<ListItem>, position: Int): Boolean
            = items[position] is GenreListItem

    override fun onBindViewHolder(items: MutableList<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val item = items[position] as GenreListItem
        (holder as ViewHolder).bind(item.item)
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_genre, parent, false)
    )

    private inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        private lateinit var currentItem: GenreItem

        init {
            view.setOnClickListener {
                checkedGenre = currentItem.value
                listener.onItemClick(currentItem, layoutPosition)
            }
        }

        fun bind(item: GenreItem) {
            currentItem = item
            view.run {
                item_title.text = item.title
                item_title.isChecked = item.value == checkedGenre
            }
        }
    }
}