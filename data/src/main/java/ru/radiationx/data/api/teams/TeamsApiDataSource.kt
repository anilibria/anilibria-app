package ru.radiationx.data.api.teams

import anilibria.api.teams.TeamsApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import ru.radiationx.data.api.teams.mapper.toDomain
import ru.radiationx.data.api.teams.models.Team
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