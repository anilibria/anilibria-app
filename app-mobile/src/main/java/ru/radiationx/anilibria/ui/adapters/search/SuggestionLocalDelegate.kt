package ru.radiationx.anilibria.ui.adapters.search

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import dev.androidbroadcast.vbpd.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemFastSearchBinding
import ru.radiationx.anilibria.model.SuggestionLocalItemState
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.SuggestionLocalListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeDelegate
import ru.radiationx.anilibria.utils.dimensions.Side
import ru.radiationx.anilibria.utils.dimensions.dimensionsApplier
import ru.radiationx.shared.ktx.android.setCompatDrawable
import ru.radiationx.shared.ktx.android.setTintColorAttr

/**
 * Created by radiationx on 13.01.18.
 */
class SuggestionLocalDelegate(
    private val clickListener: (SuggestionLocalItemState) -> Unit
) : AppAdapterDelegate<SuggestionLocalListItem, ListItem, SuggestionLocalDelegate.ViewHolder>(
    R.layout.item_fast_search,
    { it is SuggestionLocalListItem },
    { ViewHolder(it, clickListener) }
), OptimizeDelegate {

    override fun getPoolSize(): Int = 15

    override fun bindData(item: SuggestionLocalListItem, holder: ViewHolder) =
        holder.bind(item.state)

    class ViewHolder(
        itemView: View,
        private val clickListener: (SuggestionLocalItemState) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemFastSearchBinding>()

        private val dimensionsApplier by dimensionsApplier()

        init {
            binding.itemImage.scaleType = ImageView.ScaleType.CENTER
        }

        fun bind(item: SuggestionLocalItemState) {
            dimensionsApplier.applyPaddings(Side.Left, Side.Right)
            binding.itemImage.setCompatDrawable(item.icRes)
            binding.itemImage.setTintColorAttr(com.google.android.material.R.attr.colorOnSurface)
            binding.itemImage.background = null
            binding.itemTitle.text = item.title
            binding.root.setOnClickListener {
                clickListener.invoke(item)
            }
        }
    }
}