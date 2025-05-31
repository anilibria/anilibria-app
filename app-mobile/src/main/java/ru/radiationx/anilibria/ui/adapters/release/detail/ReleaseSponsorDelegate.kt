package ru.radiationx.anilibria.ui.adapters.release.detail

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import dev.androidbroadcast.vbpd.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemReleaseSponsorBinding
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseSponsorListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.data.apinext.models.ReleaseSponsor

/**
 * Created by radiationx on 21.01.18.
 */
class ReleaseSponsorDelegate(
    private val clickListener: (ReleaseSponsor) -> Unit,
) : AppAdapterDelegate<ReleaseSponsorListItem, ListItem, ReleaseSponsorDelegate.ViewHolder>(
    R.layout.item_release_sponsor,
    { it is ReleaseSponsorListItem },
    { ViewHolder(it, clickListener) }
) {

    override fun bindData(item: ReleaseSponsorListItem, holder: ViewHolder) {
        holder.bind(item.sponsor)
    }

    class ViewHolder(
        itemView: View,
        private val clickListener: (ReleaseSponsor) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemReleaseSponsorBinding>()

        fun bind(item: ReleaseSponsor) {
            binding.itemSponsorTitle.text = item.title
            binding.itemSponsorDesc.text = item.description
            binding.itemSponsorAction.text = item.urlTitle
            binding.itemSponsorAction.setOnClickListener { clickListener.invoke(item) }
            binding.itemSponsorAction.isVisible = item.urlTitle != null
        }
    }
}