package ru.radiationx.data.entity.mapper

import android.graphics.Color
import ru.radiationx.data.entity.response.team.TeamResponse
import ru.radiationx.data.entity.response.team.TeamRoleResponse
import ru.radiationx.data.entity.response.team.TeamUserResponse
import ru.radiationx.data.entity.response.team.TeamsResponse
import ru.radiationx.data.entity.domain.team.Team
import ru.radiationx.data.entity.domain.team.TeamRole
import ru.radiationx.data.entity.domain.team.TeamUser
import ru.radiationx.data.entity.domain.team.Teams
import ru.radiationx.shared.ktx.android.parseColorOrNull

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
    color = color?.mapColorToMd()?.parseColorOrNull()
)

private fun String.mapColorToMd(): String = when (this) {
    "#339966" -> "#4caf50"
    "#800000" -> "#f44336"
    "#ebd800" -> "#fdd835"
    "#ff6600" -> "#ff9800"
    "#b523c5" -> "#e91e63"
    "#000080" -> "#3f51b5"
    "#33cccc" -> "#00bcd4"
    else -> this
}