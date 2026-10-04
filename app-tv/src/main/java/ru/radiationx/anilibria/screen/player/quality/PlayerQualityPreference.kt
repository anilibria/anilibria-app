package ru.radiationx.anilibria.screen.player.quality

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.Display
import ru.radiationx.data.datasource.holders.AppPreference
import ru.radiationx.data.entity.common.PlayerQuality
import ru.radiationx.data.entity.domain.release.QualityInfo
import ru.radiationx.data.entity.domain.types.ReleaseId
import javax.inject.Inject

class PlayerQualityPreference @Inject constructor(
    context: Context,
    sharedPreferences: SharedPreferences,
) {
    private val preferences = ReleaseQualityPreferences(sharedPreferences)
    private val screenQuality = getScreenQuality(context)

    fun forRelease(releaseId: ReleaseId) = preferences.forRelease(releaseId)

    fun automaticQuality(info: QualityInfo): PlayerQuality? =
        resolvePlayerQuality(null, info, screenQuality)

    fun resolve(releaseId: ReleaseId, info: QualityInfo): PlayerQuality? =
        resolvePlayerQuality(forRelease(releaseId).value, info, screenQuality)
}

internal class ReleaseQualityPreferences(private val sharedPreferences: SharedPreferences) {
    private val preferences = mutableMapOf<ReleaseId, AppPreference<PlayerQuality?>>()

    fun forRelease(releaseId: ReleaseId): AppPreference<PlayerQuality?> =
        preferences.getOrPut(releaseId) {
            AppPreference(
                key = "tv_player_quality_release_${releaseId.id}",
                sharedPreferences = sharedPreferences,
                get = { key ->
                    val saved = getString(key, null)
                    PlayerQuality.entries.firstOrNull { it.name == saved }
                },
                set = { key, quality ->
                    if (quality == null) remove(key) else putString(key, quality.name)
                },
            )
        }
}

internal fun resolvePlayerQuality(
    manualQuality: PlayerQuality?,
    info: QualityInfo,
    screenQuality: PlayerQuality,
): PlayerQuality? {
    if (manualQuality != null && manualQuality in info.available) return manualQuality
    return info.available.filter { it.ordinal <= screenQuality.ordinal }.maxByOrNull { it.ordinal }
        ?: info.available.minByOrNull { it.ordinal }
}

@Suppress("DEPRECATION")
private fun getScreenQuality(context: Context): PlayerQuality {
    val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
        ?: return PlayerQuality.SD
    val size = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // TV UI can render at 1080p or 720p even when the video output has a higher resolution.
        display.mode.let { Point(it.physicalWidth, it.physicalHeight) }
    } else {
        Point().also { display.getRealSize(it) }
    }
    return screenPlayerQuality(size.x, size.y)
}

internal fun screenPlayerQuality(width: Int, height: Int): PlayerQuality {
    val longSide = maxOf(width, height)
    val shortSide = minOf(width, height)
    return when {
        longSide >= 1920 && shortSide >= 1080 -> PlayerQuality.FULLHD
        longSide >= 1280 && shortSide >= 720 -> PlayerQuality.HD
        else -> PlayerQuality.SD
    }
}
