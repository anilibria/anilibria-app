package ru.radiationx.anilibria.ui.fragments.search

import android.text.Html
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_fast_search.view.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.data.api.models.search.SearchItem
import ru.radiationx.anilibria.ui.adapters.BaseAdapter
import ru.radiationx.anilibria.ui.adapters.BaseViewHolder

/**
 * Created by radiationx on 24.12.17.
 */
class FastSearchAdapter : BaseAdapter<SearchItem, FastSearchAdapter.ItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ItemHolder {
        return ItemHolder(inflateLayout(parent, R.layout.item_fast_search))
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.bind(items[position])
    }

    class ItemHolder(itemView: View?) : BaseViewHolder<SearchItem>(itemView) {

        override fun bind(item: SearchItem) {
            itemView.run {
                item_title.text = Html.fromHtml(item.title)
                item_title_original.text = Html.fromHtml(item.originalTitle)
            }
        }

    }
}
