package ru.radiationx.anilibria.ui.fragments.teams.adapter

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_team_section.*
import ru.radiationx.anilibria.R
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
        override val containerView: View,
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(data: TeamSectionState) {
            tvTeamTitle.text = data.title
            tvTeamDesc.text = data.description
            tvTeamDesc.isVisible = data.description != null
        }
    }
}