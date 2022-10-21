package ru.radiationx.anilibria.ui.fragments.teams

import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.data.entity.domain.team.Team
import ru.radiationx.data.entity.domain.team.TeamUser

data class TeamSectionListItem(val data: Team) : ListItem(data.title + data.description)
data class TeamUserListItem(val data: TeamUser) : ListItem(data.nickname)
