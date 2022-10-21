package ru.radiationx.anilibria.presentation.teams

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