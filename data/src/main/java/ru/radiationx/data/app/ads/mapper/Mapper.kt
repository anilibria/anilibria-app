package ru.radiationx.data.app.ads.mapper

import ru.radiationx.data.app.ads.models.AdsConfig
import ru.radiationx.data.app.ads.models.BannerAdConfig
import ru.radiationx.data.app.ads.models.NativeAdConfig
import ru.radiationx.data.app.ads.remote.AdsConfigResponse
import ru.radiationx.data.app.ads.remote.BannerAdConfigResponse
import ru.radiationx.data.app.ads.remote.NativeAdConfigResponse


fun AdsConfigResponse.toDomain() = AdsConfig(
    appId = appId,
    mainBanner = mainBanner.toDomain(),
    feedNative = feedNative.toDomain(),
    releaseNative = releaseNative.toDomain()
)

fun BannerAdConfigResponse.toDomain() = BannerAdConfig(
    enabled = enabled,
    unitId = unitId,
    contextTags = contextTags
)

fun NativeAdConfigResponse.toDomain() = NativeAdConfig(
    enabled = enabled,
    unitId = unitId,
    timeoutMillis = timeoutMillis,
    contextTags = contextTags,
    listInsertPosition = listInsertPosition
)