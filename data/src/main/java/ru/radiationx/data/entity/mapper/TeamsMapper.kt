package ru.radiationx.data.entity.mapper

import ru.radiationx.data.entity.app.team.TeamResponse
import ru.radiationx.data.entity.app.team.TeamRoleResponse
import ru.radiationx.data.entity.app.team.TeamUserResponse
import ru.radiationx.data.entity.app.team.TeamsResponse
import ru.radiationx.data.entity.domain.team.Team
import ru.radiationx.data.entity.domain.team.TeamRole
import ru.radiationx.data.entity.domain.team.TeamUser
import ru.radiationx.data.entity.domain.team.Teams

fun TeamsResponse.toDomain() = Teams(
    headerRoles = headerRoles.map { it.toDomain() },
    teams = teams.map { it.toDomain() }
)

fun TeamResponse.toDomain() = Team(
    title = title,
    description = description,
    users = users.map { it.toDomain() }
)

fun TeamUserResponse.toDomain() = TeamUser(
    nickname = nickname,
    roles = roles.map { it.toDomain() },
    isIntern = isIntern,
    isVacation = isVacation
)

fun TeamRoleResponse.toDomain() = TeamRole(
    title = title,
    color = color
)