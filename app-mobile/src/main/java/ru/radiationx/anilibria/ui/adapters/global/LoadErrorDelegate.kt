package ru.radiationx.anilibria.ui.adapters.global

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import dev.androidbroadcast.vbpd.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemLoadErrorBinding
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.LoadErrorListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.utils.dimensions.Side
import ru.radiationx.anilibria.utils.dimensions.dimensionsApplier

/**
 * Created by radiationx on 13.01.18.
 */
class LoadErrorDelegate(
    private val retryListener: () -> Unit
) : AppAdapterDelegate<LoadErrorListItem, ListItem, LoadErrorDelegate.ViewHolder>(
    R.layout.item_load_error,
    { it is LoadErrorListItem },
    { ViewHolder(it, retryListener) }
) {

    override fun bindData(item: LoadErrorListItem, holder: ViewHolder) = holder.bind()

    class ViewHolder(
        itemView: View,
        private val listener: () -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemLoadErrorBinding>()

        private val dimensionsApplier by dimensionsApplier()

        fun bind() {
            dimensionsApplier.applyPaddings(Side.Left, Side.Right)
            binding.itemLoadErrorBtn.setOnClickListener {
                listener.invoke()
            }
        }
    }
}