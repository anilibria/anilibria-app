package ru.radiationx.anilibria.presentation.teams

import ru.radiationx.data.entity.domain.team.TeamRole

data class TeamsState(
    val hasQuery: Boolean = false,
    val headerRoles: List<TeamRole>,
    val teams: List<TeamState>
)

data class TeamState(
    val section: TeamSectionState,
    val users: List<TeamUserState>
)

data class TeamSectionState(
    val title: String,
    val description: String?
)

data class TeamUserState(
    val nickname: String,
    val color: Int?,
    val roles: List<String>,
    val tags: List<String>,
)