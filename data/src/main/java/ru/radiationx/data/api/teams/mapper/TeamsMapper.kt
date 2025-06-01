package ru.radiationx.data.api.teams.mapper

import anilibria.api.teams.models.TeamsRoleResponse
import anilibria.api.teams.models.TeamsTeamResponse
import anilibria.api.teams.models.TeamsUserResponse
import ru.radiationx.data.api.teams.models.Team
import ru.radiationx.data.api.teams.models.TeamRole
import ru.radiationx.data.api.teams.models.TeamUser

fun List<TeamsTeamResponse>.toDomain(users: List<TeamsUserResponse>): List<Team> {
    val teamIdToUserMap = users.groupBy { it.team.id }
    return map {
        it.toDomain(teamIdToUserMap[it.id].orEmpty())
    }
}

fun TeamsTeamResponse.toDomain(users: List<TeamsUserResponse>): Team {
    return Team(
        title = title,
        description = description,
        users = users.map { it.toDomain() }
    )
}

fun TeamsUserResponse.toDomain() = TeamUser(
    nickname = nickname,
    roles = roles.map { it.toDomain() },
    isIntern = isIntern,
    isVacation = isVacation
)

fun TeamsRoleResponse.toDomain() = TeamRole(
    title = title,
)

