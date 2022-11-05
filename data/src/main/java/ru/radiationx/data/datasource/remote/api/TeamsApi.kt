package ru.radiationx.data.datasource.remote.api

import com.squareup.moshi.Moshi
import org.json.JSONObject
import ru.radiationx.data.ApiClient
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.fetchResult
import ru.radiationx.data.entity.response.team.TeamsResponse
import toothpick.InjectConstructor

@InjectConstructor
class TeamsApi(
    @ApiClient private val client: IClient,
    private val apiConfig: ApiConfig,
    private val moshi: Moshi
) {

    private val dataAdapter by lazy {
        moshi.adapter(TeamsResponse::class.java)
    }

    suspend fun getTeams(): TeamsResponse {
        val args: Map<String, String> = mapOf(
            "query" to "teams"
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchResult<JSONObject>()
            .let { dataAdapter.fromJson(it.toString()) }
            .let { requireNotNull(it) }
    }
}