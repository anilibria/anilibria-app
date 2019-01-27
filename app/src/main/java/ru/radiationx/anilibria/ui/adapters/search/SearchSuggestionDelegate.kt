package ru.radiationx.anilibria.ui.adapters.search

import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import kotlinx.android.synthetic.main.item_fast_search.view.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.search.SearchItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.LoadMoreListItem
import ru.radiationx.anilibria.ui.adapters.SearchSuggestionListItem

/**
 * Created by radiationx on 13.01.18.
 */
class SearchSuggestionDelegate : AdapterDelegate<MutableList<ListItem>>() {

    override fun isForViewType(items: MutableList<ListItem>, position: Int): Boolean
            = items[position] is LoadMoreListItem

    override fun onBindViewHolder(items: MutableList<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val item = items[position] as SearchSuggestionListItem
        (holder as ViewHolder).bind(item.item)
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_fast_search, parent, false)
    )

    private inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: SearchItem) {
            view.run {
                item_title.text = Html.fromHtml(item.title)
                item_title_original.text = Html.fromHtml(item.titleEng)
            }
        }
    }
}