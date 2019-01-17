package ru.radiationx.anilibria.ui.fragments.settings

import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import ru.radiationx.anilibria.App

import ru.radiationx.anilibria.BuildConfig
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.extension.getColorFromAttr
import ru.radiationx.anilibria.model.data.holders.PreferencesHolder
import ru.radiationx.anilibria.ui.activities.updatechecker.UpdateCheckerActivity
import ru.radiationx.anilibria.utils.Utils

/**
 * Created by radiationx on 25.12.16.
 */

class SettingsFragment : BaseSettingFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)

        findPreference("quality")?.apply {
            val savedQuality = App.injections.appPreferences.getQuality()
            icon = getQualityIcon(savedQuality)
            summary = getQualityTitle(savedQuality)
            setOnPreferenceClickListener { preference ->
                val values = arrayOf(
                        PreferencesHolder.QUALITY_SD,
                        PreferencesHolder.QUALITY_HD,
                        PreferencesHolder.QUALITY_NO,
                        PreferencesHolder.QUALITY_ALWAYS
                )
                val titles = values.map { getQualityTitle(it) }.toTypedArray()
                AlertDialog.Builder(preference.context)
                        .setTitle(preference.title)
                        .setItems(titles) { dialog, which ->
                            val quality = values[which]
                            App.injections.appPreferences.setQuality(quality)
                            icon = getQualityIcon(quality)
                            summary = getQualityTitle(quality)
                        }
                        .show()
                false
            }
        }

        findPreference("player_type")?.apply {
            val savedPlayerType = App.injections.appPreferences.getPlayerType()
            icon = ContextCompat.getDrawable(this.context, R.drawable.ic_play_circle_outline)
            summary = getPlayerTypeTitle(savedPlayerType)
            setOnPreferenceClickListener { preference ->
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
                            App.injections.appPreferences.setPlayerType(playerType)
                            summary = getPlayerTypeTitle(playerType)
                        }
                        .show()
                false
            }
        }

        findPreference("about.application")?.apply {
            summary = "Версия ${BuildConfig.VERSION_NAME}"
        }

        findPreference("about.app_topic_site")?.apply {
            icon = ContextCompat.getDrawable(this.context, R.drawable.ic_anilibria)
            setOnPreferenceClickListener { preference ->
                Utils.externalLink("https://www.anilibria.tv/all/app/")
                false
            }
        }

        findPreference("about.app_topic_4pda")?.apply {
            icon = ContextCompat.getDrawable(this.context, R.drawable.ic_4pda)
            setOnPreferenceClickListener { preference ->
                Utils.externalLink("http://4pda.ru/forum/index.php?showtopic=886616")
                false
            }
        }

        /*findPreference("about.app_play_market")?.apply {
            icon = ContextCompat.getDrawable(this.context, R.drawable.ic_play_market)
            setOnPreferenceClickListener { preference ->
                Utils.externalLink("https://play.google.com/store/apps/details?id=ru.radiationx.anilibria")
                false
            }
        }*/

        findPreference("about.check_update")?.apply {
            setOnPreferenceClickListener { preference ->
                startActivity(Intent(activity, UpdateCheckerActivity::class.java).apply {
                    putExtra(UpdateCheckerActivity.ARG_FORCE, true)
                })
                false
            }
        }
    }

    private fun getQualityIcon(quality: Int): Drawable? {
        val iconRes = when (quality) {
            PreferencesHolder.QUALITY_SD -> R.drawable.ic_quality_sd
            PreferencesHolder.QUALITY_HD -> R.drawable.ic_quality_hd
            else -> {
                return null
            }
        }
        return context?.let {
            ContextCompat.getDrawable(it, iconRes)?.mutate()?.apply {
                setColorFilter(
                        it.getColorFromAttr(R.attr.base_icon),
                        PorterDuff.Mode.SRC_ATOP
                )
            }
        }
    }

    private fun getQualityTitle(quality: Int): String {
        return when (quality) {
            PreferencesHolder.QUALITY_SD -> "SD"
            PreferencesHolder.QUALITY_HD -> "HD"
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
