package ru.radiationx.data.app.preferences

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.api.releases.models.PlayerQuality
import ru.radiationx.data.player.PlayerTransport
import ru.radiationx.shared.ktx.android.PreferenceFlow
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by radiationx on 03.02.18.
 */
class PreferencesStorage @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val sharedBuildConfig: SharedBuildConfig,
) : PreferencesHolder {

    companion object {
        private const val NEW_DONATION_REMIND_KEY = "new_donation_remind_access"
        private const val RELEASE_REMIND_KEY = "release_remind"
        private const val EPISODES_IS_REVERSE_KEY = "episodes_is_reverse"
        private const val PLAYER_QUALITY_KEY = "player_quality"
        private const val PLAYER_TRANSPORT_KEY = "player_transport"
        private const val PLAY_SPEED_KEY = "play_speed"
        private const val PLAYER_SKIPS_KEY = "player_skips"
        private const val PLAYER_SKIPS_TIMER_KEY = "player_skips_timer"
        private const val PLAYER_INACTIVE_TIMER_KEY = "player_inactive_timer"
        private const val PLAYER_AUTO_PLAY_KEY = "player_auto_play"
        private const val NOTIFICATIONS_ALL_KEY = "notifications.all"
        private const val NOTIFICATIONS_SERVICE_KEY = "notifications.service"

        private val DONATION_THRESHOLD = TimeUnit.DAYS.toMillis(7)
    }

    private val speeds = listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
    private val speedsState = MutableStateFlow(speeds)

    override val newDonationRemind: PreferenceFlow<Boolean> = PreferenceFlow(
        key = NEW_DONATION_REMIND_KEY,
        sharedPreferences = sharedPreferences,
        get = { key ->
            val accessDate = getLong(key, 0L)
            val diff = System.currentTimeMillis() - accessDate
            diff > DONATION_THRESHOLD
        },
        set = { key, value ->
            val accessDate = if (value) 0L else System.currentTimeMillis()
            putLong(key, accessDate)
        }
    )

    override val releaseRemind: PreferenceFlow<Boolean> = PreferenceFlow(
        key = RELEASE_REMIND_KEY,
        sharedPreferences = sharedPreferences,
        get = { key ->
            getBoolean(key, true)
        },
        set = { key, value ->
            putBoolean(key, value)
        }
    )

    override val episodesIsReverse: PreferenceFlow<Boolean> = PreferenceFlow(
        key = EPISODES_IS_REVERSE_KEY,
        sharedPreferences = sharedPreferences,
        get = { key ->
            getBoolean(key, false)
        },
        set = { key, value ->
            putBoolean(key, value)
        }
    )

    override val playerTransport: PreferenceFlow<PlayerTransport> = PreferenceFlow(
        key = PLAYER_TRANSPORT_KEY,
        sharedPreferences = sharedPreferences,
        get = { key ->
            if (sharedBuildConfig.debug) {
                getString(key, null)?.asPlayerTransport() ?: PlayerTransport.OKHTTP
            } else {
                PlayerTransport.OKHTTP
            }
        },
        set = { key, value ->
            if (sharedBuildConfig.debug) {
                putString(key, value.asPrefString())
            }
        }
    )

    override val playerQuality: PreferenceFlow<PlayerQuality> = PreferenceFlow(
        key = PLAYER_QUALITY_KEY,
        sharedPreferences = sharedPreferences,
        get = { key ->
            getString(key, null)?.asPlayerQuality() ?: PlayerQuality.SD
        },
        set = { key, value ->
            putString(key, value.asPrefString())
        }
    )

    override val playSpeed: PreferenceFlow<Float> = PreferenceFlow(
        key = PLAY_SPEED_KEY,
        sharedPreferences = sharedPreferences,
        get = { key ->
            getFloat(key, 1.0f)
        },
        set = { key, value ->
            putFloat(key, value)
        }
    )

    override val playerSkips: PreferenceFlow<Boolean> = PreferenceFlow(
        key = PLAYER_SKIPS_KEY,
        sharedPreferences = sharedPreferences,
        get = { key ->
            getBoolean(key, true)
        },
        set = { key, value ->
            putBoolean(key, value)
        }
    )

    override val playerSkipsTimer: PreferenceFlow<Boolean> = PreferenceFlow(
        key = PLAYER_SKIPS_TIMER_KEY,
        sharedPreferences = sharedPreferences,
        get = { key ->
            getBoolean(key, true)
        },
        set = { key, value ->
            putBoolean(key, value)
        }
    )

    override val playerInactiveTimer: PreferenceFlow<Boolean> = PreferenceFlow(
        key = PLAYER_INACTIVE_TIMER_KEY,
        sharedPreferences = sharedPreferences,
        get = { key ->
            getBoolean(key, false)
        },
        set = { key, value ->
            putBoolean(key, value)
        }
    )

    override val playerAutoplay: PreferenceFlow<Boolean> = PreferenceFlow(
        key = PLAYER_AUTO_PLAY_KEY,
        sharedPreferences = sharedPreferences,
        get = { key ->
            getBoolean(key, true)
        },
        set = { key, value ->
            putBoolean(key, value)
        }
    )

    override val notificationsAll: PreferenceFlow<Boolean> = PreferenceFlow(
        key = NOTIFICATIONS_ALL_KEY,
        sharedPreferences = sharedPreferences,
        get = { key ->
            getBoolean(key, true)
        },
        set = { key, value ->
            putBoolean(key, value)
        }
    )

    override val notificationsService: PreferenceFlow<Boolean> = PreferenceFlow(
        key = NOTIFICATIONS_SERVICE_KEY,
        sharedPreferences = sharedPreferences,
        get = { key ->
            getBoolean(key, true)
        },
        set = { key, value ->
            putBoolean(key, value)
        }
    )

    override val availableSpeeds: StateFlow<List<Float>> = speedsState.asStateFlow()

    private fun String.asPlayerQuality(): PlayerQuality? {
        return when (this) {
            "sd" -> PlayerQuality.SD
            "hd" -> PlayerQuality.HD
            "fullhd" -> PlayerQuality.FULLHD
            else -> null
        }
    }

    private fun PlayerQuality.asPrefString(): String {
        return when (this) {
            PlayerQuality.SD -> "sd"
            PlayerQuality.HD -> "hd"
            PlayerQuality.FULLHD -> "fullhd"
        }
    }

    private fun String.asPlayerTransport(): PlayerTransport? {
        return when (this) {
            "system" -> PlayerTransport.SYSTEM
            "okhttp" -> PlayerTransport.OKHTTP
            "cronet" -> PlayerTransport.CRONET
            else -> null
        }
    }

    private fun PlayerTransport.asPrefString(): String {
        return when (this) {
            PlayerTransport.SYSTEM -> "system"
            PlayerTransport.OKHTTP -> "okhttp"
            PlayerTransport.CRONET -> "cronet"
        }
    }
}