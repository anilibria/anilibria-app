package ru.radiationx.data.datasource.remote.api

import com.squareup.moshi.Moshi
import ru.radiationx.data.ApiClient
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.fetchListApiResponse
import ru.radiationx.data.entity.response.other.LinkMenuResponse
import javax.inject.Inject

class MenuApi @Inject constructor(
    @ApiClient private val client: IClient,
    private val apiConfig: ApiConfig,
    private val moshi: Moshi
) {

    suspend fun getMenu(): List<LinkMenuResponse> {
        val args: Map<String, String> = mapOf(
            "query" to "link_menu"
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchListApiResponse(moshi)
    }

}