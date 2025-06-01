package ru.radiationx.anilibria.ui.fragments.settings

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.apptheme.AppThemeController
import ru.radiationx.anilibria.apptheme.AppThemeMode
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.SettingsAnalytics
import ru.radiationx.data.analytics.features.mapper.toAnalyticsQuality
import ru.radiationx.data.api.releases.models.PlayerQuality
import ru.radiationx.data.app.preferences.PreferencesHolder
import ru.radiationx.data.player.PlayerTransport
import ru.radiationx.quill.inject
import taiwa.TaiwaAction
import taiwa.bottomsheet.bottomSheetTaiwa

/**
 * Created by radiationx on 25.12.16.
 */

class SettingsFragment : BaseSettingFragment() {

    private val appPreferences by inject<PreferencesHolder>()

    private val settingsAnalytics by inject<SettingsAnalytics>()

    private val sharedBuildConfig by inject<SharedBuildConfig>()

    private val appThemeController by inject<AppThemeController>()

    private val themeTaiwa by bottomSheetTaiwa()

    private val qualityTaiwa by bottomSheetTaiwa()

    private val transportTaiwa by bottomSheetTaiwa()


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

        findPreference<SwitchPreferenceCompat>("episodes_is_reverse")?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                (newValue as? Boolean)?.also(settingsAnalytics::episodesOrderChange)
                return@setOnPreferenceChangeListener true
            }
        }

        findPreference<Preference>("app_theme")?.apply {
            setOnPreferenceClickListener {
                showThemeTaiwa()
                return@setOnPreferenceClickListener false
            }
        }

        findPreference<Preference>("player_quality")?.apply {
            setOnPreferenceClickListener {
                settingsAnalytics.qualityClick()
                showQualityTaiwa()
                false
            }
        }

        findPreference<Preference>("player_transport")?.apply {
            isVisible = sharedBuildConfig.debug
            setOnPreferenceClickListener {
                showTransportTaiwa()
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
                    .createIntent(requireContext())
                startActivity(intent)
                false
            }
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appThemeController.observeMode().onEach { mode ->
            findPreference<Preference>("app_theme")?.apply {
                summary = mode.getTitle()
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        appPreferences.playerQuality.onEach { quality ->
            findPreference<Preference>("player_quality")?.apply {
                icon = getQualityIcon(quality)
                summary = getQualityTitle(quality)
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        appPreferences.playerTransport.onEach { transport ->
            findPreference<Preference>("player_transport")?.apply {
                summary = getTransportTitle(transport)
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun showThemeTaiwa() {
        val currentValue = appThemeController.getMode()
        themeTaiwa.setContent {
            header {
                toolbar { title(getString(R.string.pref_title_theme_mode)) }
            }
            body {
                AppThemeMode.entries.forEach { mode ->
                    radioItem {
                        title(mode.getTitle())
                        select(mode == currentValue)
                        action(TaiwaAction.Close)
                        onClick { appThemeController.setMode(mode) }
                    }
                }
            }
        }
        themeTaiwa.show()
    }

    private fun showQualityTaiwa() {
        val currentValue = appPreferences.playerQuality.value
        qualityTaiwa.setContent {
            header {
                toolbar {
                    title(getString(R.string.pref_quality))
                }
            }
            body {
                PlayerQuality.entries.forEach { quality ->
                    radioItem {
                        icon(getQualityIconRes(quality))
                        title(getQualityTitle(quality))
                        select(quality == currentValue)
                        action(TaiwaAction.Close)
                        onClick {
                            settingsAnalytics.qualityChange(quality.toAnalyticsQuality())
                            appPreferences.playerQuality.value = quality
                        }
                    }
                }
            }
        }
        qualityTaiwa.show()
    }

    private fun showTransportTaiwa() {
        val currentValue = appPreferences.playerTransport.value
        transportTaiwa.setContent {
            header {
                toolbar {
                    title(getString(R.string.pref_transport))
                }
            }
            body {

                PlayerTransport.entries.forEach { transport ->
                    radioItem {
                        title(getTransportTitle(transport))
                        select(transport == currentValue)
                        action(TaiwaAction.Close)
                        onClick {
                            appPreferences.playerTransport.value = transport
                        }
                    }
                }
            }
        }
        transportTaiwa.show()
    }

    private fun getQualityIcon(quality: PlayerQuality): Drawable? {
        return ContextCompat.getDrawable(requireContext(), getQualityIconRes(quality))
    }

    private fun getQualityIconRes(quality: PlayerQuality): Int {
        return when (quality) {
            PlayerQuality.SD -> R.drawable.ic_quality_sd_base
            PlayerQuality.HD -> R.drawable.ic_quality_hd_base
            PlayerQuality.FULLHD -> R.drawable.ic_quality_full_hd_base
        }
    }

    private fun getQualityTitle(quality: PlayerQuality): String {
        return when (quality) {
            PlayerQuality.SD -> "480p"
            PlayerQuality.HD -> "720p"
            PlayerQuality.FULLHD -> "1080p"
        }
    }

    private fun AppThemeMode.getTitle(): String {
        return when (this) {
            AppThemeMode.LIGHT -> R.string.pref_value_theme_mode_light
            AppThemeMode.DARK -> R.string.pref_value_theme_mode_dark
            AppThemeMode.SYSTEM -> R.string.pref_value_theme_mode_system
        }.let { getString(it) }
    }

    private fun getTransportTitle(transport: PlayerTransport): String {
        return when (transport) {
            PlayerTransport.SYSTEM -> "Системный"
            PlayerTransport.OKHTTP -> "OkHttp"
            PlayerTransport.CRONET -> "Cronet"
        }
    }

}
