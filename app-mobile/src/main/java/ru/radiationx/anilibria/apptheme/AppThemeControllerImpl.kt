package ru.radiationx.anilibria.apptheme

import android.app.Activity
import android.app.Application
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import ru.radiationx.shared_app.common.SimpleActivityLifecycleCallbacks
import toothpick.InjectConstructor

@InjectConstructor
class AppThemeControllerImpl(
    private val application: Application,
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
                applyTheme(mode)
                modeRelay.accept(mode)
                triggerRelay.accept(Unit)
            }
        }
    }

    private val lifecycleCallbacks = object : SimpleActivityLifecycleCallbacks() {
        override fun onActivityCreated(p0: Activity?, p1: Bundle?) {
            triggerRelay.accept(Unit)
        }
    }

    init {
        applyTheme(getMode())
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        application.registerActivityLifecycleCallbacks(lifecycleCallbacks)
    }

    override fun observeTheme(): Observable<AppThemeController.AppTheme> = Observable
        .fromCallable { getAppTheme() }
        .repeatWhen { triggerRelay }
        .distinctUntilChanged()

    override fun getTheme(): AppThemeController.AppTheme = getAppTheme()

    override fun observeMode(): Observable<AppThemeController.AppThemeMode> {
        return modeRelay.hide().distinctUntilChanged()
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

    private fun getAppTheme(): AppThemeController.AppTheme = when (getMode()) {
        AppThemeController.AppThemeMode.LIGHT -> AppThemeController.AppTheme.LIGHT
        AppThemeController.AppThemeMode.DARK -> AppThemeController.AppTheme.DARK
        AppThemeController.AppThemeMode.SYSTEM -> getAppThemeByConfig()
    }

    private fun getAppThemeByConfig(): AppThemeController.AppTheme {
        val currentNightMode = application.resources.configuration.uiMode.let {
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