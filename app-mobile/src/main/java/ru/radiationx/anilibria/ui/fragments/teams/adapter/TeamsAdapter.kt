package ru.radiationx.anilibria.ui.fragments.teams.adapter

import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter
import ru.radiationx.anilibria.ui.fragments.teams.TeamSectionListItem
import ru.radiationx.anilibria.ui.fragments.teams.TeamUserListItem
import ru.radiationx.data.entity.domain.team.Teams

class TeamsAdapter : ListItemAdapter() {

    init {
        addDelegate(TeamSectionDelegate())
        addDelegate(TeamUserDelegate())
    }

    fun bindState(data: Teams) {
        val newItems = mutableListOf<ListItem>()
        data.teams.forEach { team ->
            newItems.add(TeamSectionListItem(team))
            team.users.forEach { user ->
                newItems.add(TeamUserListItem(user))
            }
        }
        items = newItems
    }
}