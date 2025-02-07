package ru.radiationx.anilibria.ui.fragments.teams.adapter

import android.view.View
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.recyclerview.widget.RecyclerView
import dev.androidbroadcast.vbpd.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemTeamsHeaderBinding
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.data.entity.domain.team.TeamRole

class TeamsHeaderDelegate(
    private val actionClickListener: () -> Unit
) : AppAdapterDelegate<TeamsHeaderListItem, ListItem, TeamsHeaderDelegate.ViewHolder>(
    R.layout.item_teams_header,
    { it is TeamsHeaderListItem },
    { ViewHolder(it, actionClickListener) }
) {

    override fun bindData(item: TeamsHeaderListItem, holder: ViewHolder) =
        holder.bind(item.data)

    class ViewHolder(
        itemView: View,
        private val actionClickListener: () -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemTeamsHeaderBinding>()

        fun bind(data: List<TeamRole>) {
            binding.tvHeaderRoles.text = buildSpannedString {
                data.mapIndexed { index, role ->
                    val color = role.color
                    if (color != null) {
                        color(color) {
                            append(role.title)
                        }
                    } else {
                        append(role.title)
                    }
                    if (index != data.lastIndex) {
                        append(", ")
                    }
                }
            }
            binding.btAction.setOnClickListener { actionClickListener.invoke() }
        }
    }
}