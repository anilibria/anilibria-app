package ru.radiationx.data.entity.mapper

import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.entity.app.feed.FeedItem
import ru.radiationx.data.entity.response.feed.FeedResponse
import ru.radiationx.data.system.ApiUtils

fun FeedResponse.toDomain(
    apiUtils: ApiUtils,
    apiConfig: ApiConfig
): FeedItem = FeedItem(
    release = release?.toDomain(apiUtils, apiConfig),
    youtube = youtube?.toDomain(apiUtils, apiConfig)
)