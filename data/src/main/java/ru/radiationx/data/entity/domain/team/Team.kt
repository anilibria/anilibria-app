package ru.radiationx.data.entity.domain.team

data class Team(
    val title: String,
    val description: String?,
    val users: List<TeamUser>
)