package ru.radiationx.data.apinext.datasources

import anilibria.api.teams.TeamsApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import ru.radiationx.data.entity.domain.team.Team
import ru.radiationx.data.entity.domain.team.TeamRole
import ru.radiationx.data.entity.domain.team.TeamUser
import ru.radiationx.data.entity.mapper.toDomain
import ru.radiationx.shared.ktx.android.parseColorOrNull
import toothpick.InjectConstructor

@InjectConstructor
class TeamsApiDataSource(
    private val api: TeamsApi
) {

    suspend fun getTeams(): List<Team> {
        return coroutineScope {
            val teamsAsync = async { api.getTeams() }
            val usersAsync = async { api.getUsers() }

            val teams = teamsAsync.await()
            val users = usersAsync.await()

            teams.toDomain(users)
        }
    }
}