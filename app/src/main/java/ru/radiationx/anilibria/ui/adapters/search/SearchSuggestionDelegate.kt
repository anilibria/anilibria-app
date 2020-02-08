package ru.radiationx.anilibria.ui.adapters.search

import android.graphics.Typeface
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.synthetic.main.item_fast_search.view.*
import ru.radiationx.anilibria.R
import ru.radiationx.data.entity.app.search.SearchItem
import ru.radiationx.data.entity.app.search.SuggestionItem
import ru.radiationx.anilibria.extension.visible
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.SearchSuggestionListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import java.util.regex.Pattern

/**
 * Created by radiationx on 13.01.18.
 */
class SearchSuggestionDelegate(
        private val clickListener: (SearchItem) -> Unit
) : AppAdapterDelegate<SearchSuggestionListItem, ListItem, SearchSuggestionDelegate.ViewHolder>(
        R.layout.item_fast_search,
        { it is SearchSuggestionListItem },
        { ViewHolder(it, clickListener) }
) {

    override fun bindData(item: SearchSuggestionListItem, holder: ViewHolder) =
            holder.bind(item.item)

    class ViewHolder(
            val view: View,
            private val clickListener: (SearchItem) -> Unit
    ) : RecyclerView.ViewHolder(view) {

        private lateinit var currentItem: SearchItem

        init {
            view.setOnClickListener {
                clickListener.invoke(currentItem)
            }
            view.item_image.scaleType = ImageView.ScaleType.CENTER_CROP
            //view.item_subtitle.visible()
        }

        fun bind(item: SuggestionItem) {
            currentItem = item
            view.run {
                ImageLoader.getInstance().cancelDisplayTask(item_image)
                ImageLoader.getInstance().displayImage(item.poster, item_image)
                setText(item_title, item, item.names[0])
                //setText(item_subtitle, item, item.names[1])
            }
        }

        fun setText(textView: TextView, item: SuggestionItem, title: String) {
            val matcher = Pattern.compile(item.query, Pattern.CASE_INSENSITIVE).matcher(title)
            val s = SpannableString(title)
            while (matcher.find()) {
                s.setSpan(StyleSpan(Typeface.BOLD), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            textView.setText(s, TextView.BufferType.SPANNABLE)
        }
    }
}