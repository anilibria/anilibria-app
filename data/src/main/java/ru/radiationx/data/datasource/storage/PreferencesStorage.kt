package ru.radiationx.data.datasource.storage

import android.content.SharedPreferences
import ru.radiationx.data.datasource.holders.AppPreference
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.entity.common.PlayerQuality
import ru.radiationx.data.entity.common.PlayerTransport
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by radiationx on 03.02.18.
 */
class PreferencesStorage @Inject constructor(
    private val sharedPreferences: SharedPreferences,
) : PreferencesHolder {

    companion object {
        private const val NEW_DONATION_REMIND_KEY = "new_donation_remind_access"
        private const val RELEASE_REMIND_KEY = "release_remind"
        private const val SEARCH_REMIND_KEY = "search_remind"
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

    override val newDonationRemind: AppPreference<Boolean> = AppPreference(
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

    override val releaseRemind: AppPreference<Boolean> = AppPreference(
        key = RELEASE_REMIND_KEY,
        sharedPreferences = sharedPreferences,
        get = { key ->
            getBoolean(key, true)
        },
        set = { key, value ->
            putBoolean(key, value)
        }
    )

    override val searchRemind: AppPreference<Boolean> = AppPreference(
        key = SEARCH_REMIND_KEY,
        sharedPreferences = sharedPreferences,
        get = { key ->
            getBoolean(key, true)
        },
        set = { key, value ->
            putBoolean(key, value)
        }
    )

    override val episodesIsReverse: AppPreference<Boolean> = AppPreference(
        key = EPISODES_IS_REVERSE_KEY,
        sharedPreferences = sharedPreferences,
        get = { key ->
            getBoolean(key, false)
        },
        set = { key, value ->
            putBoolean(key, value)
        }
    )

    override val playerTransport: AppPreference<PlayerTransport> = AppPreference(
        key = PLAYER_TRANSPORT_KEY,
        sharedPreferences = sharedPreferences,
        get = { key ->
            getString(key, null)?.asPlayerTransport() ?: PlayerTransport.OKHTTP
        },
        set = { key, value ->
            putString(key, value.asPrefString())
        }
    )

    override val playerQuality: AppPreference<PlayerQuality> = AppPreference(
        key = PLAYER_QUALITY_KEY,
        sharedPreferences = sharedPreferences,
        get = { key ->
            getString(key, null)?.asPlayerQuality() ?: PlayerQuality.SD
        },
        set = { key, value ->
            putString(key, value.asPrefString())
        }
    )

    override val playSpeed: AppPreference<Float> = AppPreference(
        key = PLAY_SPEED_KEY,
        sharedPreferences = sharedPreferences,
        get = { key ->
            getFloat(key, 1.0f)
        },
        set = { key, value ->
            putFloat(key, value)
        }
    )

    override val playerSkips: AppPreference<Boolean> = AppPreference(
        key = PLAYER_SKIPS_KEY,
        sharedPreferences = sharedPreferences,
        get = { key ->
            getBoolean(key, true)
        },
        set = { key, value ->
            putBoolean(key, value)
        }
    )

    override val playerSkipsTimer: AppPreference<Boolean> = AppPreference(
        key = PLAYER_SKIPS_TIMER_KEY,
        sharedPreferences = sharedPreferences,
        get = { key ->
            getBoolean(key, true)
        },
        set = { key, value ->
            putBoolean(key, value)
        }
    )

    override val playerInactiveTimer: AppPreference<Boolean> = AppPreference(
        key = PLAYER_INACTIVE_TIMER_KEY,
        sharedPreferences = sharedPreferences,
        get = { key ->
            getBoolean(key, false)
        },
        set = { key, value ->
            putBoolean(key, value)
        }
    )

    override val playerAutoplay: AppPreference<Boolean> = AppPreference(
        key = PLAYER_AUTO_PLAY_KEY,
        sharedPreferences = sharedPreferences,
        get = { key ->
            getBoolean(key, true)
        },
        set = { key, value ->
            putBoolean(key, value)
        }
    )

    override val notificationsAll: AppPreference<Boolean> = AppPreference(
        key = NOTIFICATIONS_ALL_KEY,
        sharedPreferences = sharedPreferences,
        get = { key ->
            getBoolean(key, true)
        },
        set = { key, value ->
            putBoolean(key, value)
        }
    )

    override val notificationsService: AppPreference<Boolean> = AppPreference(
        key = NOTIFICATIONS_SERVICE_KEY,
        sharedPreferences = sharedPreferences,
        get = { key ->
            getBoolean(key, true)
        },
        set = { key, value ->
            putBoolean(key, value)
        }
    )

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