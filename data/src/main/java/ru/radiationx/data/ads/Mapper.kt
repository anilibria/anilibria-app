package ru.radiationx.data.ads

import ru.radiationx.data.ads.domain.AdsConfig
import ru.radiationx.data.ads.domain.BannerAdConfig
import ru.radiationx.data.ads.domain.NativeAdConfig
import ru.radiationx.data.ads.remote.AdsConfigResponse
import ru.radiationx.data.ads.remote.BannerAdConfigResponse
import ru.radiationx.data.ads.remote.NativeAdConfigResponse


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