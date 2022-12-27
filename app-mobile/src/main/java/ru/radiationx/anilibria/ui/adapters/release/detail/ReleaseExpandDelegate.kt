package ru.radiationx.anilibria.ui.adapters.release.detail

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemReleaseExpandBinding
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseExpandListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeDelegate

/**
 * Created by radiationx on 13.01.18.
 */
class ReleaseExpandDelegate(
    private val clickListener: (ReleaseExpandListItem) -> Unit
) : AppAdapterDelegate<ReleaseExpandListItem, ListItem, ReleaseExpandDelegate.ViewHolder>(
    R.layout.item_release_expand,
    { it is ReleaseExpandListItem },
    { ViewHolder(it, clickListener) }
), OptimizeDelegate {

    override fun getPoolSize(): Int = 20

    override fun bindData(item: ReleaseExpandListItem, holder: ViewHolder) =
        holder.bind(item)

    class ViewHolder(
        itemView: View,
        private val clickListener: (ReleaseExpandListItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemReleaseExpandBinding>()

        fun bind(item: ReleaseExpandListItem) {
            binding.itemExpandTitle.text = item.title
            binding.itemExpandTitle.setOnClickListener { clickListener.invoke(item) }
        }
    }
}