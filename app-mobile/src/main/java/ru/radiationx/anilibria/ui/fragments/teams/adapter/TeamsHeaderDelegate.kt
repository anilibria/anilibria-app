package ru.radiationx.anilibria.ui.fragments.teams.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import dev.androidbroadcast.vbpd.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemTeamsHeaderBinding
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate

class TeamsHeaderDelegate(
    private val actionClickListener: () -> Unit
) : AppAdapterDelegate<TeamsHeaderListItem, ListItem, TeamsHeaderDelegate.ViewHolder>(
    R.layout.item_teams_header,
    { it is TeamsHeaderListItem },
    { ViewHolder(it, actionClickListener) }
) {

    override fun bindData(item: TeamsHeaderListItem, holder: ViewHolder) =
        holder.bind()

    class ViewHolder(
        itemView: View,
        private val actionClickListener: () -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemTeamsHeaderBinding>()

        fun bind() {
            binding.btAction.setOnClickListener { actionClickListener.invoke() }
        }
    }
}