package ru.radiationx.data.entity.mapper

import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.entity.domain.youtube.YoutubeItem
import ru.radiationx.data.entity.response.youtube.YoutubeResponse
import ru.radiationx.data.system.ApiUtils

fun YoutubeResponse.toDomain(
    apiUtils: ApiUtils,
    apiConfig: ApiConfig
) = YoutubeItem(
    id = id,
    title = apiUtils.escapeHtml(title),
    image = image?.appendBaseUrl(apiConfig.baseImagesUrl),
    vid = vid,
    views = views,
    comments = comments,
    timestamp = timestamp
)