package ru.radiationx.anilibria.ui.fragments.settings

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import ru.radiationx.anilibria.BuildConfig
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.SettingsAnalytics
import ru.radiationx.data.analytics.features.UpdaterAnalytics
import ru.radiationx.data.analytics.features.mapper.toAnalyticsPlayer
import ru.radiationx.data.analytics.features.mapper.toAnalyticsQuality
import ru.radiationx.data.analytics.features.model.AnalyticsAppTheme
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.datasource.remote.Api
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.quill.inject
import ru.radiationx.shared.ktx.android.getCompatDrawable

/**
 * Created by radiationx on 25.12.16.
 */

class SettingsFragment : BaseSettingFragment() {

    private val appPreferences by inject<PreferencesHolder>()

    private val apiConfig by inject<ApiConfig>()

    private val authRepository by inject<AuthRepository>()

    private val errorHandler by inject<IErrorHandler>()

    private val settingsAnalytics by inject<SettingsAnalytics>()

    private val updaterAnalytics by inject<UpdaterAnalytics>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)

        findPreference<SwitchPreferenceCompat>("notifications.all")?.apply {
            setOnPreferenceChangeListener { preference, newValue ->
                (newValue as? Boolean)?.also(settingsAnalytics::notificationMainChange)
                return@setOnPreferenceChangeListener true
            }
        }

        findPreference<SwitchPreferenceCompat>("notifications.service")?.apply {
            setOnPreferenceChangeListener { preference, newValue ->
                (newValue as? Boolean)?.also(settingsAnalytics::notificationSystemChange)
                return@setOnPreferenceChangeListener true
            }
        }

        findPreference<SwitchPreferenceCompat>("app_theme_dark")?.apply {
            setOnPreferenceChangeListener { preference, newValue ->
                (newValue as? Boolean)?.also { isDark ->
                    val theme = if (isDark) {
                        AnalyticsAppTheme.DARK
                    } else {
                        AnalyticsAppTheme.LIGHT
                    }
                    settingsAnalytics.themeChange(theme)
                }
                return@setOnPreferenceChangeListener true
            }
        }

        findPreference<SwitchPreferenceCompat>("episodes_is_reverse")?.apply {
            setOnPreferenceChangeListener { preference, newValue ->
                (newValue as? Boolean)?.also(settingsAnalytics::episodesOrderChange)
                return@setOnPreferenceChangeListener true
            }
        }

        findPreference<Preference>("quality")?.apply {
            val savedQuality = appPreferences.getQuality()
            icon = getQualityIcon(savedQuality)
            summary = getQualityTitle(savedQuality)
            setOnPreferenceClickListener { preference ->
                settingsAnalytics.qualityClick()
                val values = arrayOf(
                    PreferencesHolder.QUALITY_SD,
                    PreferencesHolder.QUALITY_HD,
                    PreferencesHolder.QUALITY_FULL_HD,
                    PreferencesHolder.QUALITY_NO,
                    PreferencesHolder.QUALITY_ALWAYS
                )
                val titles = values.map { getQualityTitle(it) }.toTypedArray()
                AlertDialog.Builder(preference.context)
                    .setTitle(preference.title)
                    .setItems(titles) { _, which ->
                        val quality = values[which]
                        settingsAnalytics.qualityChange(quality.toAnalyticsQuality())
                        appPreferences.setQuality(quality)
                        icon = getQualityIcon(quality)
                        summary = getQualityTitle(quality)
                    }
                    .show()
                false
            }
        }

        findPreference<Preference>("player_type")?.apply {
            val savedPlayerType = appPreferences.getPlayerType()
            icon = this.context.getCompatDrawable(R.drawable.ic_play_circle_outline)
            summary = getPlayerTypeTitle(savedPlayerType)
            setOnPreferenceClickListener { preference ->
                settingsAnalytics.playerClick()
                val values = arrayOf(
                    PreferencesHolder.PLAYER_TYPE_EXTERNAL,
                    PreferencesHolder.PLAYER_TYPE_INTERNAL,
                    PreferencesHolder.PLAYER_TYPE_NO,
                    PreferencesHolder.PLAYER_TYPE_ALWAYS
                )
                val titles = values.map { getPlayerTypeTitle(it) }.toTypedArray()
                AlertDialog.Builder(preference.context)
                    .setTitle(preference.title)
                    .setItems(titles) { dialog, which ->
                        val playerType = values[which]
                        settingsAnalytics.playerChange(playerType.toAnalyticsPlayer())
                        appPreferences.setPlayerType(playerType)
                        summary = getPlayerTypeTitle(playerType)
                    }
                    .show()
                false
            }
        }

        findPreference<Preference>("about.application")?.apply {
            val appendix = if (Api.STORE_APP_IDS.contains(BuildConfig.APPLICATION_ID)) {
                " для Play Market"
            } else {
                ""
            }
            summary = "Версия ${BuildConfig.VERSION_NAME}$appendix"
        }

        findPreference<Preference>("about.check_update")?.apply {
            setOnPreferenceClickListener {
                settingsAnalytics.checkUpdatesClick()
                val intent = Screens.AppUpdateScreen(true, AnalyticsConstants.screen_settings)
                    .getActivityIntent(requireContext())
                startActivity(intent)
                false
            }
        }
    }

    private fun getQualityIcon(quality: Int): Drawable? {
        val iconRes = when (quality) {
            PreferencesHolder.QUALITY_SD -> R.drawable.ic_quality_sd_base
            PreferencesHolder.QUALITY_HD -> R.drawable.ic_quality_hd_base
            PreferencesHolder.QUALITY_FULL_HD -> R.drawable.ic_quality_full_hd_base
            else -> return null
        }
        return ContextCompat.getDrawable(requireContext(), iconRes)
    }

    private fun getQualityTitle(quality: Int): String {
        return when (quality) {
            PreferencesHolder.QUALITY_SD -> "480p"
            PreferencesHolder.QUALITY_HD -> "720p"
            PreferencesHolder.QUALITY_FULL_HD -> "1080p"
            PreferencesHolder.QUALITY_NO -> "Не выбрано"
            PreferencesHolder.QUALITY_ALWAYS -> "Спрашивать всегда"
            else -> ""
        }
    }

    private fun getPlayerTypeTitle(playerType: Int): String {
        return when (playerType) {
            PreferencesHolder.PLAYER_TYPE_EXTERNAL -> "Внешний плеер"
            PreferencesHolder.PLAYER_TYPE_INTERNAL -> "Внутренний плеер"
            PreferencesHolder.PLAYER_TYPE_NO -> "Не выбрано"
            PreferencesHolder.PLAYER_TYPE_ALWAYS -> "Спрашивать всегда"
            else -> ""
        }
    }
}
