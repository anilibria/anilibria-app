package ru.radiationx.anilibria.ui.adapters.search

import android.graphics.Typeface
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.synthetic.main.item_fast_search.view.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.search.SearchItem
import ru.radiationx.anilibria.entity.app.search.SuggestionItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.SearchSuggestionListItem
import java.util.regex.Pattern

/**
 * Created by radiationx on 13.01.18.
 */
class SearchSuggestionDelegate(
        private val clickListener: (SearchItem) -> Unit
) : AdapterDelegate<MutableList<ListItem>>() {

    override fun isForViewType(items: MutableList<ListItem>, position: Int): Boolean = items[position] is SearchSuggestionListItem

    override fun onBindViewHolder(items: MutableList<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        (items[position] as SearchSuggestionListItem).also {
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
            view.item_image.scaleType = ImageView.ScaleType.CENTER_CROP
        }

        fun bind(item: SuggestionItem) {
            currentItem = item
            view.run {
                ImageLoader.getInstance().cancelDisplayTask(item_image)
                ImageLoader.getInstance().displayImage(item.poster, item_image)
                val title = item.names.joinToString(" / ")
                val matcher = Pattern.compile(item.query, Pattern.CASE_INSENSITIVE).matcher(title)
                val s = SpannableString(title)
                while (matcher.find()) {
                    s.setSpan(StyleSpan(Typeface.BOLD), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                item_title.setText(s, TextView.BufferType.SPANNABLE)
            }
        }
    }
}