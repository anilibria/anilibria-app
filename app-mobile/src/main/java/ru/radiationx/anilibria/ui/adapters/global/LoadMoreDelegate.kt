package ru.radiationx.anilibria.ui.adapters.global

import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemLoadMoreBinding
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.LoadMoreListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate

/**
 * Created by radiationx on 13.01.18.
 */
class LoadMoreDelegate(
    private val listener: (() -> Unit)?
) : AppAdapterDelegate<LoadMoreListItem, ListItem, LoadMoreDelegate.ViewHolder>(
    R.layout.item_load_more,
    { it is LoadMoreListItem },
    { ViewHolder(it, listener) }
) {

    override fun bindData(item: LoadMoreListItem, holder: ViewHolder) = holder.bind(item.needNotify)

    class ViewHolder(
        itemView: View,
        private val listener: (() -> Unit)?
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemLoadMoreBinding>()

        init {
            binding.itemLoadMoreBtn.isGone = true
            binding.itemLoadMoreContainer.isVisible = true
        }

        fun bind(needNotify: Boolean) {
            if (needNotify) {
                listener?.invoke()
            }
        }
    }
}