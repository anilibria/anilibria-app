package ru.radiationx.data.repository

import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.api.YoutubeApi
import ru.radiationx.data.entity.app.Paginated
import ru.radiationx.data.entity.app.youtube.YoutubeItem
import ru.radiationx.data.entity.mapper.toDomain
import ru.radiationx.data.system.ApiUtils
import javax.inject.Inject

class YoutubeRepository @Inject constructor(
    private val youtubeApi: YoutubeApi,
    private val apiUtils: ApiUtils,
    private val apiConfig: ApiConfig
) {

    suspend fun getYoutubeList(page: Int): Paginated<YoutubeItem> = youtubeApi
        .getYoutubeList(page)
        .toDomain { it.toDomain(apiUtils, apiConfig) }
}