package ru.radiationx.data.datasource.storage

import android.content.SharedPreferences
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import ru.radiationx.data.datasource.holders.PreferencesHolder
import javax.inject.Inject

/**
 * Created by radiationx on 03.02.18.
 */
class PreferencesStorage @Inject constructor(
    private val sharedPreferences: SharedPreferences
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

    private val qualityRelay = BehaviorRelay.createDefault<Int>(getQuality())
    private val playSpeedRelay = BehaviorRelay.createDefault<Float>(playSpeed)
    private val notificationsAllRelay = BehaviorRelay.createDefault<Boolean>(notificationsAll)
    private val notificationsServiceRelay =
        BehaviorRelay.createDefault<Boolean>(notificationsService)
    private val searchRemindRelay = BehaviorRelay.createDefault<Boolean>(searchRemind)
    private val releaseRemindRelay = BehaviorRelay.createDefault<Boolean>(releaseRemind)
    private val episodesIsReverseRelay = BehaviorRelay.createDefault<Boolean>(episodesIsReverse)
    private val newDonationRemindRelay = BehaviorRelay.createDefault<Boolean>(newDonationRemind)

    // Важно, чтобы было вынесено именно в поле
    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            NOTIFICATIONS_ALL_KEY -> notificationsAllRelay.accept(notificationsAll)
            NOTIFICATIONS_SERVICE_KEY -> notificationsServiceRelay.accept(notificationsService)
            QUALITY_KEY -> qualityRelay.accept(getQuality())
            PLAY_SPEED_KEY -> playSpeedRelay.accept(playSpeed)
            SEARCH_REMIND_KEY -> searchRemindRelay.accept(searchRemind)
            RELEASE_REMIND_KEY -> releaseRemindRelay.accept(releaseRemind)
            EPISODES_IS_REVERSE_KEY -> episodesIsReverseRelay.accept(episodesIsReverse)
            NEW_DONATION_REMIND_KEY -> newDonationRemindRelay.accept(newDonationRemind)
        }
    }

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun observeNewDonationRemind(): Observable<Boolean> = newDonationRemindRelay.hide()

    override var newDonationRemind: Boolean
        get() = sharedPreferences.getBoolean(NEW_DONATION_REMIND_KEY, true)
        set(value) = sharedPreferences.edit().putBoolean(NEW_DONATION_REMIND_KEY, value).apply()

    override fun observeReleaseRemind(): Observable<Boolean> = releaseRemindRelay.hide()

    override var releaseRemind: Boolean
        get() = sharedPreferences.getBoolean(RELEASE_REMIND_KEY, true)
        set(value) = sharedPreferences.edit().putBoolean(RELEASE_REMIND_KEY, value).apply()

    override fun observeSearchRemind(): Observable<Boolean> = searchRemindRelay.hide()

    override var searchRemind: Boolean
        get() = sharedPreferences.getBoolean(SEARCH_REMIND_KEY, true)
        set(value) = sharedPreferences.edit().putBoolean(SEARCH_REMIND_KEY, value).apply()

    override fun observeEpisodesIsReverse(): Observable<Boolean> = episodesIsReverseRelay.hide()

    override val episodesIsReverse: Boolean
        get() = sharedPreferences.getBoolean(EPISODES_IS_REVERSE_KEY, false)

    override fun getQuality(): Int {
        return sharedPreferences.getInt(QUALITY_KEY, PreferencesHolder.QUALITY_NO)
    }

    override fun setQuality(value: Int) {
        sharedPreferences.edit().putInt(QUALITY_KEY, value).apply()
    }

    override fun observeQuality(): Observable<Int> = qualityRelay.hide()

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

    override fun observePlaySpeed(): Observable<Float> = playSpeedRelay.hide()

    override var pipControl: Int
        get() = sharedPreferences.getInt(PIP_CONTROL_KEY, PreferencesHolder.PIP_BUTTON)
        set(value) {
            sharedPreferences.edit().putInt(PIP_CONTROL_KEY, value).apply()
        }

    override var notificationsAll: Boolean
        get() = sharedPreferences.getBoolean(NOTIFICATIONS_ALL_KEY, true)
        set(value) = sharedPreferences.edit().putBoolean(NOTIFICATIONS_ALL_KEY, value).apply()

    override fun observeNotificationsAll(): Observable<Boolean> = notificationsAllRelay.hide()

    override var notificationsService: Boolean
        get() = sharedPreferences.getBoolean(NOTIFICATIONS_SERVICE_KEY, true)
        set(value) = sharedPreferences.edit().putBoolean(NOTIFICATIONS_SERVICE_KEY, value).apply()

    override fun observeNotificationsService(): Observable<Boolean> =
        notificationsServiceRelay.hide()
}