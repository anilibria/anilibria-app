package ru.radiationx.data.datasource.remote.api

import com.squareup.moshi.Moshi
import io.reactivex.Single
import org.json.JSONObject
import ru.radiationx.data.ApiClient
import ru.radiationx.data.datasource.remote.ApiResponse
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.entity.app.team.TeamResponse
import ru.radiationx.data.entity.app.team.TeamsResponse
import ru.radiationx.data.entity.domain.team.Teams
import ru.radiationx.data.entity.mapper.toDomain
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

    fun getTeams(): Single<TeamsResponse> {
        val args: Map<String, String> = mapOf(
            "query" to "donation_details"
        )
        return client.post(apiConfig.apiUrl, args)
            .compose(ApiResponse.fetchResult<JSONObject>())
            .map { dataAdapter.fromJson(it.toString()) }
    }
}