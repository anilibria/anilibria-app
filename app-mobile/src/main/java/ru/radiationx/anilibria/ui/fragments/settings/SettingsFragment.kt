package ru.radiationx.anilibria.ui.fragments.settings

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.SettingsAnalytics
import ru.radiationx.data.analytics.features.mapper.toAnalyticsQuality
import ru.radiationx.data.analytics.features.model.AnalyticsAppTheme
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.entity.common.PlayerQuality
import ru.radiationx.quill.inject
import ru.radiationx.shared.ktx.android.showWithLifecycle

/**
 * Created by radiationx on 25.12.16.
 */

class SettingsFragment : BaseSettingFragment() {

    private val appPreferences by inject<PreferencesHolder>()

    private val settingsAnalytics by inject<SettingsAnalytics>()

    private val sharedBuildConfig by inject<SharedBuildConfig>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)

        findPreference<SwitchPreferenceCompat>("notifications.all")?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                (newValue as? Boolean)?.also(settingsAnalytics::notificationMainChange)
                return@setOnPreferenceChangeListener true
            }
        }

        findPreference<SwitchPreferenceCompat>("notifications.service")?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                (newValue as? Boolean)?.also(settingsAnalytics::notificationSystemChange)
                return@setOnPreferenceChangeListener true
            }
        }

        findPreference<SwitchPreferenceCompat>("app_theme_dark")?.apply {
            setOnPreferenceChangeListener { _, newValue ->
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
            setOnPreferenceChangeListener { _, newValue ->
                (newValue as? Boolean)?.also(settingsAnalytics::episodesOrderChange)
                return@setOnPreferenceChangeListener true
            }
        }

        findPreference<Preference>("player_quality")?.apply {
            val savedQuality = appPreferences.playerQuality.value
            icon = getQualityIcon(savedQuality)
            summary = getQualityTitle(savedQuality)
            setOnPreferenceClickListener { preference ->
                settingsAnalytics.qualityClick()
                val values = PlayerQuality.entries
                val titles = values.map { getQualityTitle(it) }.toTypedArray()
                AlertDialog.Builder(preference.context)
                    .setTitle(preference.title)
                    .setItems(titles) { _, which ->
                        val quality = values[which]
                        settingsAnalytics.qualityChange(quality.toAnalyticsQuality())
                        appPreferences.playerQuality.value = quality
                        icon = getQualityIcon(quality)
                        summary = getQualityTitle(quality)
                    }
                    .showWithLifecycle(viewLifecycleOwner)
                false
            }
        }

        findPreference<Preference>("about.application")?.apply {
            summary = "Версия ${sharedBuildConfig.versionName} (${sharedBuildConfig.buildDate})"
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

    private fun getQualityIcon(quality: PlayerQuality): Drawable? {
        val iconRes = when (quality) {
            PlayerQuality.SD -> R.drawable.ic_quality_sd_base
            PlayerQuality.HD -> R.drawable.ic_quality_hd_base
            PlayerQuality.FULLHD -> R.drawable.ic_quality_full_hd_base
        }
        return ContextCompat.getDrawable(requireContext(), iconRes)
    }

    private fun getQualityTitle(quality: PlayerQuality): String {
        return when (quality) {
            PlayerQuality.SD -> "480p"
            PlayerQuality.HD -> "720p"
            PlayerQuality.FULLHD -> "1080p"
        }
    }

}
