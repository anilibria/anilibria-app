package ru.radiationx.anilibria.ui.adapters.feed

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemAppInfoCardBinding
import ru.radiationx.anilibria.ui.adapters.AppInfoCardListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.fragments.feed.FeedAppWarning
import ru.radiationx.anilibria.utils.dimensions.Side
import ru.radiationx.anilibria.utils.dimensions.dimensionsApplier

/**
 * Created by radiationx on 13.01.18.
 */
class AppInfoCardDelegate(
    private val clickListener: (FeedAppWarning) -> Unit,
    private val closeClickListener: (FeedAppWarning) -> Unit
) : AppAdapterDelegate<AppInfoCardListItem, ListItem, AppInfoCardDelegate.ViewHolder>(
    R.layout.item_app_info_card,
    { it is AppInfoCardListItem },
    { ViewHolder(it, clickListener, closeClickListener) }
) {

    override fun bindData(item: AppInfoCardListItem, holder: ViewHolder) =
        holder.bind(item.warning)

    class ViewHolder(
        itemView: View,
        private val clickListener: (FeedAppWarning) -> Unit,
        private val closeClickListener: (FeedAppWarning) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemAppInfoCardBinding>()

        private val dimensionsApplier by dimensionsApplier()

        fun bind(warning: FeedAppWarning) {
            dimensionsApplier.applyMargins(Side.Left, Side.Right)
            binding.root.setOnClickListener { clickListener.invoke(warning) }
            binding.btClose.setOnClickListener { closeClickListener.invoke(warning) }
            binding.tvContent.text = warning.title
        }
    }
}