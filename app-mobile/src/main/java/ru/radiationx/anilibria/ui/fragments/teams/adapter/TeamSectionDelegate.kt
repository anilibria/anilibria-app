package ru.radiationx.anilibria.ui.fragments.teams.adapter

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemTeamSectionBinding
import ru.radiationx.anilibria.presentation.teams.TeamSectionState
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate

class TeamSectionDelegate :
    AppAdapterDelegate<TeamSectionListItem, ListItem, TeamSectionDelegate.ViewHolder>(
        R.layout.item_team_section,
        { it is TeamSectionListItem },
        { ViewHolder(it) }
    ) {

    override fun bindData(item: TeamSectionListItem, holder: ViewHolder) =
        holder.bind(item.data)

    class ViewHolder(
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemTeamSectionBinding>()

        fun bind(data: TeamSectionState) {
            binding.tvTeamTitle.text = data.title
            binding.tvTeamDesc.text = data.description
            binding.tvTeamDesc.isVisible = data.description != null
        }
    }
}