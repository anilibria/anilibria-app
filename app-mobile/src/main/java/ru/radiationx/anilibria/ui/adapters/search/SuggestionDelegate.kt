package ru.radiationx.anilibria.ui.adapters.search

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemFastSearchBinding
import ru.radiationx.anilibria.model.SuggestionItemState
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.SuggestionListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.utils.dimensions.Side
import ru.radiationx.anilibria.utils.dimensions.dimensionsApplier
import ru.radiationx.shared_app.imageloader.showImageUrl

/**
 * Created by radiationx on 13.01.18.
 */
class SuggestionDelegate(
    private val clickListener: (SuggestionItemState) -> Unit,
) : AppAdapterDelegate<SuggestionListItem, ListItem, SuggestionDelegate.ViewHolder>(
    R.layout.item_fast_search,
    { it is SuggestionListItem },
    { ViewHolder(it, clickListener) }
) {

    override fun bindData(item: SuggestionListItem, holder: ViewHolder) = holder.bind(item.state)

    class ViewHolder(
        itemView: View,
        private val clickListener: (SuggestionItemState) -> Unit,
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemFastSearchBinding>()

        private val dimensionsApplier by dimensionsApplier()

        init {
            binding.itemImage.scaleType = ImageView.ScaleType.CENTER_CROP
        }

        fun bind(state: SuggestionItemState) {
            dimensionsApplier.applyPaddings(Side.Left, Side.Right)
            binding.itemImage.showImageUrl(state.poster)
            setTitle(state)

            binding.root.setOnClickListener {
                clickListener.invoke(state)
            }
        }

        private fun setTitle(state: SuggestionItemState) {
            val spannableTitle = SpannableString(state.title)
            state.matchRanges.filterNot { it.isEmpty() }.forEach {
                val span = StyleSpan(Typeface.BOLD)
                val flags = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                val endIndex = (it.last + 1).coerceAtMost(state.title.length)
                spannableTitle.setSpan(span, it.first, endIndex, flags)
            }
            binding.itemTitle.setText(spannableTitle, TextView.BufferType.SPANNABLE)
        }
    }
}