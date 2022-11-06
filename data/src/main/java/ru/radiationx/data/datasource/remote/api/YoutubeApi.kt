package ru.radiationx.data.datasource.remote.api

import com.squareup.moshi.Moshi
import ru.radiationx.data.ApiClient
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.fetchPaginatedApiResponse
import ru.radiationx.data.entity.response.PaginatedResponse
import ru.radiationx.data.entity.response.youtube.YoutubeResponse
import javax.inject.Inject

class YoutubeApi @Inject constructor(
    @ApiClient private val client: IClient,
    private val apiConfig: ApiConfig,
    private val moshi: Moshi
) {

    suspend fun getYoutubeList(page: Int): PaginatedResponse<YoutubeResponse> {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "youtube",
            "page" to page.toString()
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchPaginatedApiResponse<YoutubeResponse>(moshi)
    }
}