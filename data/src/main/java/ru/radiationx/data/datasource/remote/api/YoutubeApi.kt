package ru.radiationx.data.datasource.remote.api

import com.squareup.moshi.Moshi
import ru.radiationx.data.ApiClient
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.fetchApiResponse
import ru.radiationx.data.datasource.remote.parsers.YoutubeParser
import ru.radiationx.data.entity.app.Paginated
import ru.radiationx.data.entity.app.youtube.YoutubeItem
import ru.radiationx.data.entity.mapper.toDomain
import ru.radiationx.data.entity.response.PaginatedResponse
import ru.radiationx.data.entity.response.youtube.YoutubeResponse
import ru.radiationx.data.system.ApiUtils
import javax.inject.Inject

class YoutubeApi @Inject constructor(
    @ApiClient private val client: IClient,
    private val youtubeParser: YoutubeParser,
    private val apiConfig: ApiConfig,
    private val apiUtils: ApiUtils,
    private val moshi: Moshi
) {

    suspend fun getYoutubeList(page: Int): Paginated<List<YoutubeItem>> {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "youtube",
            "page" to page.toString()
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchApiResponse<PaginatedResponse<List<YoutubeResponse>>>(moshi)
            .toDomain { data ->
                data.map { it.toDomain(apiUtils, apiConfig) }
            }
    }
}