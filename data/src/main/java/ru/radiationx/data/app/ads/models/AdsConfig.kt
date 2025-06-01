package ru.radiationx.data.app.ads.models

data class AdsConfig(
    val appId: String,
    val mainBanner: BannerAdConfig,
    val feedNative: NativeAdConfig,
    val releaseNative: NativeAdConfig,
)

data class NativeAdConfig(
    val enabled: Boolean,
    val unitId: String,
    val timeoutMillis: Long,
    val contextTags: List<String>,
    val listInsertPosition: Int,
)

data class BannerAdConfig(
    val enabled: Boolean,
    val unitId: String,
    val contextTags: List<String>,
)
