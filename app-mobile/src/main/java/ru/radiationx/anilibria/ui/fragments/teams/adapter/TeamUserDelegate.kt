package ru.radiationx.anilibria.ui.fragments.teams.adapter

import android.view.View
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_team_user.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.presentation.teams.TeamUserState
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate

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

        fun bind(data: TeamUserState) {
            tvTeamUser.text = buildSpannedString {
                if (data.color != null) {
                    color(data.color) {
                        append(data.nickname)
                    }
                } else {
                    append(data.nickname)
                }
                append(" – ")
                append(data.roles.joinToString())
            }
            tvTeamUserInfo.text = data.tags.joinToString()
            tvTeamUserInfo.isVisible = data.tags.isNotEmpty()
        }
    }
}