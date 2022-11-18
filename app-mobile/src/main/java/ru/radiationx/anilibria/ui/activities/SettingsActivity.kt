package ru.radiationx.anilibria.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.fragments.settings.SettingsFragment
import ru.radiationx.shared_app.di.injectDependencies


/**
 * Created by radiationx on 25.12.16.
 */

class SettingsActivity : BaseActivity(R.layout.activity_settings) {

    override fun onCreate(savedInstanceState: Bundle?) {
        this.injectDependencies()
        setTheme(R.style.PreferencesDayNightAppTheme)
        super.onCreate(savedInstanceState)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowTitleEnabled(true)
            actionBar.title = "Настройки"
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_content, SettingsFragment())
            .commit()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home)
            finish()
        return true
    }

    companion object {

        fun getIntent(context: Context): Intent {
            return Intent(context, SettingsActivity::class.java)
        }
    }
}
