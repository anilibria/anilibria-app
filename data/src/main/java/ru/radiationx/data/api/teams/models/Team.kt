package ru.radiationx.data.api.teams.models

data class Team(
    val title: String,
    val description: String?,
    val users: List<TeamUser>
)