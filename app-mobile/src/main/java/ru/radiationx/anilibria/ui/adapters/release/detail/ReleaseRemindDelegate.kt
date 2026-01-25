package ru.radiationx.anilibria.ui.adapters.release.detail

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import dev.androidbroadcast.vbpd.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemReleaseRemindBinding
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseRemindListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.utils.dimensions.Side
import ru.radiationx.anilibria.utils.dimensions.dimensionsApplier

/**
 * Created by radiationx on 21.01.18.
 */
class ReleaseRemindDelegate(
    private val itemListener: () -> Unit
) : AppAdapterDelegate<ReleaseRemindListItem, ListItem, ReleaseRemindDelegate.ViewHolder>(
    R.layout.item_release_remind,
    { it is ReleaseRemindListItem },
    { ViewHolder(it, itemListener) }
) {

    override fun bindData(item: ReleaseRemindListItem, holder: ViewHolder) = holder.bind(item.text)

    class ViewHolder(
        itemView: View,
        private val itemListener: () -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemReleaseRemindBinding>()

        private val dimensionsApplier by dimensionsApplier()

        init {
            binding.remindClose.setOnClickListener {
                itemListener.invoke()
            }
        }

        fun bind(item: String) {
            dimensionsApplier.applyPaddings(Side.Left, Side.Right)
            binding.itemTitle.text = item
        }
    }

}