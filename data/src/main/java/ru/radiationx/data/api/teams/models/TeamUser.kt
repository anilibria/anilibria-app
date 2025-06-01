package ru.radiationx.data.api.teams.models

data class TeamUser(
    val nickname: String,
    val roles: List<TeamRole>,
    val isIntern: Boolean,
    val isVacation: Boolean
)