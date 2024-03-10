package ru.radiationx.data.entity.domain.release

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import ru.radiationx.data.entity.common.PlayerQuality

@Parcelize
data class QualityInfo(
    val urlSd: String?,
    val urlHd: String?,
    val urlFullHd: String?,
) : Parcelable {

    @IgnoredOnParcel
    val available = buildSet(3) {
        if (!urlSd.isNullOrEmpty()) add(PlayerQuality.SD)
        if (!urlHd.isNullOrEmpty()) add(PlayerQuality.HD)
        if (!urlFullHd.isNullOrEmpty()) add(PlayerQuality.FULLHD)
    }

    operator fun contains(quality: PlayerQuality): Boolean {
        return quality in available
    }

    fun getActualFor(quality: PlayerQuality): PlayerQuality? {
        var actual: PlayerQuality? = quality

        if (actual == PlayerQuality.FULLHD && actual !in available) {
            actual = PlayerQuality.HD
        }
        if (actual == PlayerQuality.HD && actual !in available) {
            actual = PlayerQuality.SD
        }
        if (actual == PlayerQuality.SD && actual !in available) {
            actual = null
        }
        if (actual == null) {
            actual = available.firstOrNull()
        }
        return actual
    }

    fun getUrlFor(quality: PlayerQuality): String? {
        val url = when (getActualFor(quality)) {
            PlayerQuality.SD -> urlSd
            PlayerQuality.HD -> urlHd
            PlayerQuality.FULLHD -> urlFullHd
            null -> null
        }
        return url
    }

    fun getSafeUrlFor(quality: PlayerQuality): String {
        return getUrlFor(quality) ?: "https://127.0.0.1/fallback.m3u8"
    }
}