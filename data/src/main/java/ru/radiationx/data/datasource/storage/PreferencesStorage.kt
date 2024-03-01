package ru.radiationx.data.datasource.storage

import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.entity.common.PlayerQuality
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by radiationx on 03.02.18.
 */
class PreferencesStorage @Inject constructor(
    private val prefs: SharedPreferences,
) : PreferencesHolder {

    companion object {
        private const val NEW_DONATION_REMIND_KEY = "new_donation_remind_access"
        private const val RELEASE_REMIND_KEY = "release_remind"
        private const val SEARCH_REMIND_KEY = "search_remind"
        private const val EPISODES_IS_REVERSE_KEY = "episodes_is_reverse"
        private const val PLAYER_QUALITY_KEY = "player_quality"
        private const val PLAY_SPEED_KEY = "play_speed"
        private const val PLAYER_SKIPS_KEY = "player_skips"
        private const val PLAYER_SKIPS_TIMER_KEY = "player_skips_timer"
        private const val NOTIFICATIONS_ALL_KEY = "notifications.all"
        private const val NOTIFICATIONS_SERVICE_KEY = "notifications.service"

        private val DONATION_THRESHOLD = TimeUnit.DAYS.toMillis(7)
    }

    private val playerQualityRelay by lazy { MutableStateFlow(playerQuality) }
    private val playSpeedRelay by lazy { MutableStateFlow(playSpeed) }
    private val playerSkipsRelay by lazy { MutableStateFlow(playerSkips) }
    private val playerSkipsTimerRelay by lazy { MutableStateFlow(playerSkipsTimer) }
    private val notificationsAllRelay by lazy { MutableStateFlow(notificationsAll) }
    private val notificationsServiceRelay by lazy { MutableStateFlow(notificationsService) }
    private val searchRemindRelay by lazy { MutableStateFlow(searchRemind) }
    private val releaseRemindRelay by lazy { MutableStateFlow(releaseRemind) }
    private val episodesIsReverseRelay by lazy { MutableStateFlow(episodesIsReverse) }
    private val newDonationRemindRelay by lazy { MutableStateFlow(newDonationRemind) }

    // Важно, чтобы было вынесено именно в поле
    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            NOTIFICATIONS_ALL_KEY -> notificationsAllRelay.value = notificationsAll
            NOTIFICATIONS_SERVICE_KEY -> notificationsServiceRelay.value = notificationsService
            PLAYER_QUALITY_KEY -> playerQualityRelay.value = playerQuality
            PLAY_SPEED_KEY -> playSpeedRelay.value = playSpeed
            PLAYER_SKIPS_KEY -> playerSkipsRelay.value = playerSkips
            PLAYER_SKIPS_TIMER_KEY -> playerSkipsTimerRelay.value = playerSkipsTimer
            SEARCH_REMIND_KEY -> searchRemindRelay.value = searchRemind
            RELEASE_REMIND_KEY -> releaseRemindRelay.value = releaseRemind
            EPISODES_IS_REVERSE_KEY -> episodesIsReverseRelay.value = episodesIsReverse
            NEW_DONATION_REMIND_KEY -> newDonationRemindRelay.value = newDonationRemind
        }
    }

    private val sharedPreferences by lazy {
        prefs.registerOnSharedPreferenceChangeListener(listener)
        prefs
    }

    override fun observeNewDonationRemind(): Flow<Boolean> = newDonationRemindRelay

    override var newDonationRemind: Boolean
        get() {
            val accessDate = sharedPreferences.getLong(NEW_DONATION_REMIND_KEY, 0L)
            val diff = System.currentTimeMillis() - accessDate
            return diff > DONATION_THRESHOLD
        }
        set(value) {
            val accessDate = if (value) 0L else System.currentTimeMillis()
            sharedPreferences.edit().putLong(NEW_DONATION_REMIND_KEY, accessDate).apply()
        }

    override fun observeReleaseRemind(): Flow<Boolean> = releaseRemindRelay

    override var releaseRemind: Boolean
        get() = sharedPreferences.getBoolean(RELEASE_REMIND_KEY, true)
        set(value) = sharedPreferences.edit().putBoolean(RELEASE_REMIND_KEY, value).apply()

    override fun observeSearchRemind(): Flow<Boolean> = searchRemindRelay

    override var searchRemind: Boolean
        get() = sharedPreferences.getBoolean(SEARCH_REMIND_KEY, true)
        set(value) = sharedPreferences.edit().putBoolean(SEARCH_REMIND_KEY, value).apply()

    override fun observeEpisodesIsReverse(): Flow<Boolean> = episodesIsReverseRelay

    override val episodesIsReverse: Boolean
        get() = sharedPreferences.getBoolean(EPISODES_IS_REVERSE_KEY, false)

    override var playerQuality: PlayerQuality
        get() = sharedPreferences.getString(PLAYER_QUALITY_KEY, null)
            ?.asPlayerQuality()
            ?: PlayerQuality.SD
        set(value) {
            sharedPreferences.edit().putString(PLAYER_QUALITY_KEY, value.asPrefString()).apply()
        }

    override fun observePlayerQuality(): Flow<PlayerQuality> = playerQualityRelay

    override var playSpeed: Float
        get() = sharedPreferences.getFloat(PLAY_SPEED_KEY, 1.0f)
        set(value) {
            sharedPreferences.edit().putFloat(PLAY_SPEED_KEY, value).apply()
        }

    override fun observePlaySpeed(): Flow<Float> = playSpeedRelay

    override var playerSkips: Boolean
        get() = sharedPreferences.getBoolean(PLAYER_SKIPS_KEY, true)
        set(value) = sharedPreferences.edit().putBoolean(PLAYER_SKIPS_KEY, value).apply()

    override fun observePlayerSkipsTimer(): Flow<Boolean> = playerSkipsTimerRelay

    override var playerSkipsTimer: Boolean
        get() = sharedPreferences.getBoolean(PLAYER_SKIPS_TIMER_KEY, true)
        set(value) = sharedPreferences.edit().putBoolean(PLAYER_SKIPS_TIMER_KEY, value).apply()

    override var notificationsAll: Boolean
        get() = sharedPreferences.getBoolean(NOTIFICATIONS_ALL_KEY, true)
        set(value) = sharedPreferences.edit().putBoolean(NOTIFICATIONS_ALL_KEY, value).apply()

    override fun observeNotificationsAll(): Flow<Boolean> = notificationsAllRelay

    override var notificationsService: Boolean
        get() = sharedPreferences.getBoolean(NOTIFICATIONS_SERVICE_KEY, true)
        set(value) = sharedPreferences.edit().putBoolean(NOTIFICATIONS_SERVICE_KEY, value).apply()

    override fun observeNotificationsService(): Flow<Boolean> =
        notificationsServiceRelay

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
}