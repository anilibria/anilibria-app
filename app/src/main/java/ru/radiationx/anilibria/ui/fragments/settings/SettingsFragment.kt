package ru.radiationx.anilibria.ui.fragments.settings

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.preference.Preference
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.radiationx.anilibria.BuildConfig
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.activities.updatechecker.UpdateCheckerActivity

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

        findPreference("about.app_faq")?.apply {
            icon = ContextCompat.getDrawable(this.context, R.drawable.ic_anilibria)
            setOnPreferenceClickListener { preference ->
                //IntentHandler.externalIntent("http://4pda.ru/forum/index.php?s=&showtopic=820313&view=findpost&p=64077514")
                false
            }
        }


        findPreference("about.app_topic")?.apply {
            icon = ContextCompat.getDrawable(this.context, R.drawable.ic_anilibria)
            setOnPreferenceClickListener { preference ->
                //IntentHandler.externalIntent("https://4pda.ru/forum/index.php?showtopic=820313")
                false
            }
        }

        findPreference("about.check_update")
                .setOnPreferenceClickListener { preference ->
                    startActivity(Intent(activity, UpdateCheckerActivity::class.java))
                    false
                }
    }
}
