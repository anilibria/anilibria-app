package ru.radiationx.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.radiationx.data.datasource.holders.TeamsHolder
import ru.radiationx.data.datasource.remote.api.TeamsApi
import ru.radiationx.data.entity.domain.team.Teams
import ru.radiationx.data.entity.mapper.toDomain
import toothpick.InjectConstructor

@InjectConstructor
class TeamsRepository(
    private val teamsApi: TeamsApi,
    private val teamsHolder: TeamsHolder,
) {

    suspend fun requestUpdate() = teamsApi
        .getTeams()
        .also { teamsHolder.save(it) }

    fun observeTeams(): Flow<Teams> = teamsHolder
        .observe()
        .map { it.toDomain() }
}