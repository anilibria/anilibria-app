package ru.radiationx.data.datasource.remote.api

import com.squareup.moshi.Moshi
import ru.radiationx.data.ApiClient
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.fetchApiResponse
import ru.radiationx.data.entity.response.team.TeamsResponse
import javax.inject.Inject

class TeamsApi @Inject constructor(
    @ApiClient private val client: IClient,
    private val apiConfig: ApiConfig,
    private val moshi: Moshi
) {

    suspend fun getTeams(): TeamsResponse {
        val args: Map<String, String> = mapOf(
            "query" to "teams"
        )
        return client
            .post(apiConfig.apiUrl, args)
            .fetchApiResponse(moshi)
    }
}