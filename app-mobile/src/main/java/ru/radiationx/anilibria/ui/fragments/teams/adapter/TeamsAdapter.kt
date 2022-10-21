package ru.radiationx.anilibria.ui.fragments.teams.adapter

import ru.radiationx.anilibria.presentation.teams.TeamState
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter

class TeamsAdapter : ListItemAdapter() {

    init {
        addDelegate(TeamSectionDelegate())
        addDelegate(TeamUserDelegate())
    }

    fun bindState(data: List<TeamState>) {
        val newItems = mutableListOf<ListItem>()
        data.forEach { team ->
            newItems.add(TeamSectionListItem(team.section))
            team.users.forEach { user ->
                newItems.add(TeamUserListItem(user))
            }
        }
        items = newItems
    }
}