package ru.radiationx.anilibria.apptheme

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import toothpick.InjectConstructor

@InjectConstructor
class AppThemeControllerImpl(
    private val context: Context,
    private val sharedPreferences: SharedPreferences
) : AppThemeController {

    companion object {
        private const val APP_THEME_KEY = "app_theme"
        private const val APP_THEME_LEGACY_KEY = "app_theme_dark"
    }

    private val modeRelay by lazy { BehaviorRelay.createDefault(getMode()) }
    private val triggerRelay by lazy { PublishRelay.create<Unit>() }

    // Важно, чтобы было вынесено именно в поле
    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            APP_THEME_KEY -> {
                val mode = getMode()
                Log.d("kekeke","prefs updated $mode")
                applyTheme(mode)
                modeRelay.accept(mode)
            }
        }
    }

    init {
        applyTheme(getMode())
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun observeTheme(): Observable<AppThemeController.AppTheme> = observeMode()
        .map { getAppThemeByConfig() }
        .repeatWhen { triggerRelay }

    override fun getTheme(): AppThemeController.AppTheme = getAppThemeByConfig()

    override fun updateTheme() {
        triggerRelay.accept(Unit)
    }

    override fun observeMode(): Observable<AppThemeController.AppThemeMode> {
        return modeRelay.hide()
    }

    override fun getMode(): AppThemeController.AppThemeMode {
        return sharedPreferences.getString(APP_THEME_KEY, null)
            ?.let { prefMode ->
                AppThemeController.AppThemeMode.values().find { it.value == prefMode }
            }
            ?: getLegacyThemePrefs().toMode()
    }

    override fun setMode(mode: AppThemeController.AppThemeMode) {
        sharedPreferences.edit {
            putString(APP_THEME_KEY, mode.value)
        }
    }

    private fun getLegacyThemePrefs(): AppThemeController.AppTheme {
        val isDark = sharedPreferences.getBoolean(APP_THEME_LEGACY_KEY, false)
        return if (isDark) {
            AppThemeController.AppTheme.DARK
        } else {
            AppThemeController.AppTheme.LIGHT
        }
    }

    private fun getAppThemeByConfig(): AppThemeController.AppTheme {
        val currentNightMode = context.resources.configuration.uiMode.let {
            it and Configuration.UI_MODE_NIGHT_MASK
        }
        return if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            AppThemeController.AppTheme.DARK
        } else {
            AppThemeController.AppTheme.LIGHT
        }
    }

    private fun applyTheme(mode: AppThemeController.AppThemeMode) {
        val delegateMode = when (mode) {
            AppThemeController.AppThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            AppThemeController.AppThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            AppThemeController.AppThemeMode.SYSTEM -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                } else {
                    AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
                }
            }
        }
        AppCompatDelegate.setDefaultNightMode(delegateMode)
    }

    private fun AppThemeController.AppTheme.toMode() = when (this) {
        AppThemeController.AppTheme.LIGHT -> AppThemeController.AppThemeMode.LIGHT
        AppThemeController.AppTheme.DARK -> AppThemeController.AppThemeMode.DARK
    }
}