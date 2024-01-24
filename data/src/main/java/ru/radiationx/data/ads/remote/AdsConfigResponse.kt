package ru.radiationx.data.ads.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AdsConfigResponse(
    @Json(name = "appId") val appId: String,
    @Json(name = "mainBanner") val mainBanner: BannerAdConfigResponse,
    @Json(name = "feedNative") val feedNative: NativeAdConfigResponse,
    @Json(name = "releaseNative") val releaseNative: NativeAdConfigResponse,
)

@JsonClass(generateAdapter = true)
data class NativeAdConfigResponse(
    @Json(name = "enabled") val enabled: Boolean,
    @Json(name = "unitId") val unitId: String,
    @Json(name = "timeoutMillis") val timeoutMillis: Long,
    @Json(name = "contextTags") val contextTags: List<String>,
    @Json(name = "listInsertPosition") val listInsertPosition: Int,
)

@JsonClass(generateAdapter = true)
data class BannerAdConfigResponse(
    @Json(name = "enabled") val enabled: Boolean,
    @Json(name = "unitId") val unitId: String,
    @Json(name = "contextTags") val contextTags: List<String>,
)
