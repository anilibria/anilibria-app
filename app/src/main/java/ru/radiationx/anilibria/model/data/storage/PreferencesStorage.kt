package ru.radiationx.anilibria.model.data.storage

import android.content.SharedPreferences
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import ru.radiationx.anilibria.di.qualifier.DefaultPreferences
import ru.radiationx.anilibria.model.data.holders.AppThemeHolder
import ru.radiationx.anilibria.model.data.holders.PreferencesHolder
import javax.inject.Inject

/**
 * Created by radiationx on 03.02.18.
 */
class PreferencesStorage @Inject constructor(
        private val sharedPreferences: SharedPreferences
) : PreferencesHolder, AppThemeHolder {

    companion object {
        private const val RELEASE_REMIND_KEY = "release_remind"
        private const val SEARCH_REMIND_KEY = "search_remind"
        private const val EPISODES_IS_REVERSE_KEY = "episodes_is_reverse"
        private const val QUALITY_KEY = "quality"
        private const val PLAYER_TYPE_KEY = "player_type"
        private const val PLAY_SPEED_KEY = "play_speed"
        private const val PIP_CONTROL_KEY = "pip_control"
        private const val APP_THEME_KEY = "app_theme_dark"
    }

    private val appThemeRelay = BehaviorRelay.createDefault<AppThemeHolder.AppTheme>(getTheme())

    // Важно, чтобы было вынесено именно в поле
    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { preferences, key ->
        when (key) {
            APP_THEME_KEY -> {
                appThemeRelay.accept(getTheme())
            }
        }
    }

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun getReleaseRemind(): Boolean {
        return sharedPreferences.getBoolean(RELEASE_REMIND_KEY, true)
    }

    override fun setReleaseRemind(state: Boolean) {
        sharedPreferences.edit().putBoolean(RELEASE_REMIND_KEY, state).apply()
    }

    override fun getSearchRemind(): Boolean {
        return sharedPreferences.getBoolean(SEARCH_REMIND_KEY, true)
    }

    override fun setSearchRemind(state: Boolean) {
        sharedPreferences.edit().putBoolean(SEARCH_REMIND_KEY, state).apply()
    }

    override fun getEpisodesIsReverse(): Boolean {
        return sharedPreferences.getBoolean(EPISODES_IS_REVERSE_KEY, false)
    }

    override fun getQuality(): Int {
        return sharedPreferences.getInt(QUALITY_KEY, PreferencesHolder.QUALITY_NO)
    }

    override fun setQuality(value: Int) {
        sharedPreferences.edit().putInt(QUALITY_KEY, value).apply()
    }

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

    override var pipControl: Int
        get() = sharedPreferences.getInt(PIP_CONTROL_KEY, PreferencesHolder.PIP_BUTTON)
        set(value) {
            sharedPreferences.edit().putInt(PIP_CONTROL_KEY, value).apply()
        }

    override fun getTheme(): AppThemeHolder.AppTheme {
        val isDark = sharedPreferences.getBoolean(APP_THEME_KEY, false)
        return if (isDark) {
            AppThemeHolder.AppTheme.DARK
        } else {
            AppThemeHolder.AppTheme.LIGHT
        }
    }

    override fun observeTheme(): Observable<AppThemeHolder.AppTheme> = appThemeRelay.hide()
}