package ru.radiationx.anilibria.ui.adapters.search

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_fast_search.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.model.SuggestionLocalItemState
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.SuggestionLocalListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeDelegate
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
        override val containerView: View,
        private val clickListener: (SuggestionLocalItemState) -> Unit
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        init {
            item_image.scaleType = ImageView.ScaleType.CENTER
        }

        fun bind(item: SuggestionLocalItemState) {
            item_image.setCompatDrawable(item.icRes)
            item_image.setTintColorAttr(R.attr.colorOnSurface)
            item_image.background = null
            item_title.text = item.title
            containerView.setOnClickListener {
                clickListener.invoke(item)
            }
        }
    }
}