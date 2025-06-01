package ru.radiationx.data.api.teams

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TeamsRepository @Inject constructor(
    private val api: TeamsApiDataSource
) {

    suspend fun getTeams() = withContext(Dispatchers.IO) {
        api.getTeams()
    }
}