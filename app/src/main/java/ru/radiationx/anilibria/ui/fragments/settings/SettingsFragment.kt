package ru.radiationx.anilibria.ui.fragments.settings

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat

import ru.radiationx.anilibria.BuildConfig
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.activities.updatechecker.UpdateCheckerActivity
import ru.radiationx.anilibria.utils.Utils

/**
 * Created by radiationx on 25.12.16.
 */

class SettingsFragment : BaseSettingFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)

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

        findPreference("about.app_play_market")?.apply {
            icon = ContextCompat.getDrawable(this.context, R.drawable.ic_play_market)
            setOnPreferenceClickListener { preference ->
                Utils.externalLink("https://play.google.com/store/apps/details?id=ru.radiationx.anilibria")
                false
            }
        }

        findPreference("about.check_update")?.apply {
            setOnPreferenceClickListener { preference ->
                startActivity(Intent(activity, UpdateCheckerActivity::class.java).apply {
                    putExtra(UpdateCheckerActivity.ARG_FORCE, true)
                })
                false
            }
        }
    }
}
