package ru.radiationx.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.radiationx.data.apinext.datasources.TeamsApiDataSource
import javax.inject.Inject

class TeamsRepository @Inject constructor(
    private val api: TeamsApiDataSource
) {

    suspend fun getTeams() = withContext(Dispatchers.IO) {
        api.getTeams()
    }
}