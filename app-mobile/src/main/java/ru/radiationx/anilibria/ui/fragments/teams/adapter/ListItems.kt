package ru.radiationx.anilibria.ui.fragments.teams.adapter

import ru.radiationx.anilibria.presentation.teams.TeamSectionState
import ru.radiationx.anilibria.presentation.teams.TeamUserState
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.data.entity.domain.team.TeamRole

data class TeamsHeaderListItem(val data: List<TeamRole>) : ListItem(data)
data class TeamSectionListItem(val data: TeamSectionState) : ListItem(data)
data class TeamUserListItem(val data: TeamUserState) : ListItem(data)
