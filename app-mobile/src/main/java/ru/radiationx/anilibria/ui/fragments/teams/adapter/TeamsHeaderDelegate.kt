package ru.radiationx.anilibria.ui.fragments.teams.adapter

import android.view.View
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_teams_header.*
import ru.radiationx.anilibria.R
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
        override val containerView: View,
        private val actionClickListener: () -> Unit
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(data: List<TeamRole>) {
            tvHeaderRoles.text = buildSpannedString {
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
            btAction.setOnClickListener { actionClickListener.invoke() }
        }
    }
}