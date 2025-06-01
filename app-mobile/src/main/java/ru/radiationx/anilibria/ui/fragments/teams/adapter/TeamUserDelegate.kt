package ru.radiationx.anilibria.ui.fragments.teams.adapter

import android.view.View
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import dev.androidbroadcast.vbpd.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemTeamUserBinding
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.fragments.teams.TeamUserState

class TeamUserDelegate :
    AppAdapterDelegate<TeamUserListItem, ListItem, TeamUserDelegate.ViewHolder>(
        R.layout.item_team_user,
        { it is TeamUserListItem },
        { ViewHolder(it) }
    ) {

    override fun bindData(item: TeamUserListItem, holder: ViewHolder) =
        holder.bind(item.data)

    class ViewHolder(
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemTeamUserBinding>()

        fun bind(data: TeamUserState) {
            binding.tvTeamUser.text = buildSpannedString {
                append(data.nickname)
                if (data.roles.isNotEmpty()) {
                    append(" – ")
                    append(data.roles.joinToString())
                }
            }
            binding.tvTeamUserInfo.text = data.tags.joinToString()
            binding.tvTeamUserInfo.isVisible = data.tags.isNotEmpty()
        }
    }
}