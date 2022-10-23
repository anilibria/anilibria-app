package ru.radiationx.data.datasource.storage

import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import ru.radiationx.data.datasource.holders.PreferencesHolder
import javax.inject.Inject

/**
 * Created by radiationx on 03.02.18.
 */
class PreferencesStorage @Inject constructor(
    private val prefs: SharedPreferences
) : PreferencesHolder {

    companion object {
        private const val NEW_DONATION_REMIND_KEY = "new_donation_remind"
        private const val RELEASE_REMIND_KEY = "release_remind"
        private const val SEARCH_REMIND_KEY = "search_remind"
        private const val EPISODES_IS_REVERSE_KEY = "episodes_is_reverse"
        private const val QUALITY_KEY = "quality"
        private const val PLAYER_TYPE_KEY = "player_type"
        private const val PLAY_SPEED_KEY = "play_speed"
        private const val PIP_CONTROL_KEY = "pip_control"
        private const val NOTIFICATIONS_ALL_KEY = "notifications.all"
        private const val NOTIFICATIONS_SERVICE_KEY = "notifications.service"
    }

    private val qualityRelay by lazy { MutableStateFlow(getQuality()) }
    private val playSpeedRelay by lazy { MutableStateFlow(playSpeed) }
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
            QUALITY_KEY -> qualityRelay.value = getQuality()
            PLAY_SPEED_KEY -> playSpeedRelay.value = playSpeed
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
        get() = sharedPreferences.getBoolean(NEW_DONATION_REMIND_KEY, true)
        set(value) = sharedPreferences.edit().putBoolean(NEW_DONATION_REMIND_KEY, value).apply()

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

    override fun getQuality(): Int {
        return sharedPreferences.getInt(QUALITY_KEY, PreferencesHolder.QUALITY_NO)
    }

    override fun setQuality(value: Int) {
        sharedPreferences.edit().putInt(QUALITY_KEY, value).apply()
    }

    override fun observeQuality(): Flow<Int> = qualityRelay

    override fun getPlayerType(): Int {
        return sharedPreferences.getInt(PLAYER_TYPE_KEY, PreferencesHolder.PLAYER_TYPE_NO)
    }

    override fun setPlayerType(value: Int) {
        sharedPreferences.edit().putInt(PLAYER_TYPE_KEY, value).apply()
    }

    override var playSpeed: Float
        get() = sharedPreferences.getFloat(PLAY_SPEED_KEY, 1.0f)
        set(value) {
            sharedPreferences.edit().putFloat(PLAY_SPEED_KEY, value).apply()
        }

    override fun observePlaySpeed(): Flow<Float> = playSpeedRelay

    override var pipControl: Int
        get() = sharedPreferences.getInt(PIP_CONTROL_KEY, PreferencesHolder.PIP_BUTTON)
        set(value) {
            sharedPreferences.edit().putInt(PIP_CONTROL_KEY, value).apply()
        }

    override var notificationsAll: Boolean
        get() = sharedPreferences.getBoolean(NOTIFICATIONS_ALL_KEY, true)
        set(value) = sharedPreferences.edit().putBoolean(NOTIFICATIONS_ALL_KEY, value).apply()

    override fun observeNotificationsAll(): Flow<Boolean> = notificationsAllRelay

    override var notificationsService: Boolean
        get() = sharedPreferences.getBoolean(NOTIFICATIONS_SERVICE_KEY, true)
        set(value) = sharedPreferences.edit().putBoolean(NOTIFICATIONS_SERVICE_KEY, value).apply()

    override fun observeNotificationsService(): Flow<Boolean> =
        notificationsServiceRelay
}