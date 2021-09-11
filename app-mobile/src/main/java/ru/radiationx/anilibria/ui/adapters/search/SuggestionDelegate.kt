package ru.radiationx.anilibria.ui.adapters.search

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_fast_search.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.model.SuggestionItemState
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.SuggestionListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate

/**
 * Created by radiationx on 13.01.18.
 */
class SuggestionDelegate(
    private val clickListener: (SuggestionItemState) -> Unit
) : AppAdapterDelegate<SuggestionListItem, ListItem, SuggestionDelegate.ViewHolder>(
    R.layout.item_fast_search,
    { it is SuggestionListItem },
    { ViewHolder(it, clickListener) }
) {

    override fun bindData(item: SuggestionListItem, holder: ViewHolder) = holder.bind(item.state)

    class ViewHolder(
        override val containerView: View,
        private val clickListener: (SuggestionItemState) -> Unit
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        init {
            item_image.scaleType = ImageView.ScaleType.CENTER_CROP
        }

        fun bind(state: SuggestionItemState) {
            ImageLoader.getInstance().cancelDisplayTask(item_image)
            ImageLoader.getInstance().displayImage(state.poster, item_image)
            setTitle(state)

            containerView.setOnClickListener {
                clickListener.invoke(state)
            }
        }

        private fun setTitle(state: SuggestionItemState) {
            val spannableTitle = SpannableString(state.title)
            state.matchRanges.forEach {
                val span = StyleSpan(Typeface.BOLD)
                val flags = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                spannableTitle.setSpan(span, it.first, it.last, flags)
            }
            item_title.setText(spannableTitle, TextView.BufferType.SPANNABLE)
        }
    }
}