package ru.radiationx.anilibria.ui.fragments.teams.adapter

import android.graphics.Color
import android.view.View
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_team_user.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.fragments.teams.TeamUserListItem
import ru.radiationx.data.entity.domain.team.TeamUser

class TeamUserDelegate :
    AppAdapterDelegate<TeamUserListItem, ListItem, TeamUserDelegate.ViewHolder>(
        R.layout.item_team_user,
        { it is TeamUserListItem },
        { ViewHolder(it) }
    ) {

    override fun bindData(item: TeamUserListItem, holder: ViewHolder) =
        holder.bind(item.data)

    class ViewHolder(
        override val containerView: View,
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(data: TeamUser) {
            val primaryRole = data.roles.firstOrNull { it.color != null }
            val primaryColor = primaryRole?.color?.let { Color.parseColor(it) }
            val roles = data.roles.joinToString { it.title }
            tvTeamUser.text = buildSpannedString {
                if (primaryColor != null) {
                    color(primaryColor) {
                        append(data.nickname)
                    }
                } else {
                    append(data.nickname)
                }
                append(" – ")
                append(roles)
            }
        }
    }
}