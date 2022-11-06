package ru.radiationx.data.entity.mapper

import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.entity.domain.release.TorrentItem
import ru.radiationx.data.entity.response.release.TorrentResponse

fun TorrentResponse.toDomain(
    apiConfig: ApiConfig
): TorrentItem = TorrentItem(
    id = id,
    hash = hash,
    leechers = leechers,
    seeders = seeders,
    completed = completed,
    quality = quality,
    series = series,
    size = size,
    url = url.appendBaseUrl(apiConfig.baseImagesUrl),
    date = date.secToDate()
)