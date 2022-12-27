package ru.radiationx.data.entity.mapper

import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.entity.domain.feed.FeedItem
import ru.radiationx.data.entity.domain.types.FeedId
import ru.radiationx.data.entity.response.feed.FeedResponse
import ru.radiationx.data.system.ApiUtils

fun FeedResponse.toDomain(
    apiUtils: ApiUtils,
    apiConfig: ApiConfig
): FeedItem {
    val domainRelease = release?.toDomain(apiUtils, apiConfig)
    val domainYoutube = youtube?.toDomain(apiUtils, apiConfig)
    return FeedItem(
        id = FeedId(domainRelease?.id, domainYoutube?.id),
        release = domainRelease,
        youtube = domainYoutube
    )
}