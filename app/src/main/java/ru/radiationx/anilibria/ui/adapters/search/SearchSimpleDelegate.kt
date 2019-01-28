package ru.radiationx.anilibria.ui.adapters.search

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.support.v4.widget.ImageViewCompat
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import com.lapism.searchview.SearchView
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.synthetic.main.item_fast_simple_search.view.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.search.FastSearchItem
import ru.radiationx.anilibria.extension.getColorFromAttr
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.SearchSimpleListItem
import ru.radiationx.anilibria.ui.adapters.SearchSuggestionListItem
import java.util.regex.Pattern

/**
 * Created by radiationx on 13.01.18.
 */
class SearchSimpleDelegate : AdapterDelegate<MutableList<ListItem>>() {

    override fun isForViewType(items: MutableList<ListItem>, position: Int): Boolean = items[position] is SearchSimpleListItem

    override fun onBindViewHolder(items: MutableList<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val item = items[position] as SearchSimpleListItem
        (holder as ViewHolder).bind(item.icRes, item.title, item.query)
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_fast_simple_search, parent, false)
    )

    private inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        init {
            view.setOnClickListener {
                Log.e("loalao", "onclick")
            }
        }

        fun bind(icRes: Int, title: String, query: String) {
            view.apply {
                ImageViewCompat.setImageTintList(item_icon, ColorStateList.valueOf(context.getColorFromAttr(R.attr.base_icon)))
                item_icon.setImageDrawable(ContextCompat.getDrawable(item_icon.context, icRes))
                item_title.text = title
            }
        }
    }
}