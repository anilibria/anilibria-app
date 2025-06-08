package ru.radiationx.anilibria.ui.fragments.teams.adapter

import ru.radiationx.anilibria.extension.setAndAwaitItems
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter
import ru.radiationx.anilibria.ui.fragments.teams.TeamsState

class TeamsAdapter(
    actionClickListener: () -> Unit
) : ListItemAdapter() {

    init {
        addDelegate(TeamsHeaderDelegate(actionClickListener))
        addDelegate(TeamSectionDelegate())
        addDelegate(TeamUserDelegate())
    }

    suspend fun bindState(data: TeamsState) {
        val newItems = mutableListOf<ListItem>()
        if (!data.hasQuery) {
            newItems.add(TeamsHeaderListItem())
        }
        data.teams.forEach { team ->
            newItems.add(TeamSectionListItem(team.section))
            team.users.forEach { user ->
                newItems.add(TeamUserListItem(user))
            }
        }
        setAndAwaitItems(newItems)
    }
}