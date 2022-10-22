package ru.radiationx.data.entity.domain.team

data class TeamUser(
    val nickname: String,
    val roles: List<TeamRole>,
    val isIntern: Boolean,
    val isVacation: Boolean
)