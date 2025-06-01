package ru.radiationx.anilibria.ui.fragments.teams.adapter

import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.fragments.teams.TeamSectionState
import ru.radiationx.anilibria.ui.fragments.teams.TeamUserState

data class TeamsHeaderListItem(val tag: String = "header") : ListItem(tag)
data class TeamSectionListItem(val data: TeamSectionState) : ListItem(data)
data class TeamUserListItem(val data: TeamUserState) : ListItem(data)
