package ru.radiationx.anilibria.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem

import javax.inject.Inject

import io.reactivex.disposables.CompositeDisposable
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.di.extensions.*
import ru.radiationx.anilibria.extension.*
import ru.radiationx.anilibria.model.data.holders.AppThemeHolder
import ru.radiationx.anilibria.ui.fragments.settings.SettingsFragment


/**
 * Created by radiationx on 25.12.16.
 */

class SettingsActivity : BaseActivity() {

    @Inject
    lateinit var appThemeHolder: AppThemeHolder

    private val disposables = CompositeDisposable()

    private lateinit var currentAppTheme: AppThemeHolder.AppTheme
    override fun onCreate(savedInstanceState: Bundle?) {
        this.injectDependencies()
        super.onCreate(savedInstanceState)
        currentAppTheme = appThemeHolder.getTheme()
        setTheme(currentAppTheme.getPrefStyleRes())
        setContentView(R.layout.activity_settings)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowTitleEnabled(true)
            actionBar.title = "Настройки"
        }

        supportFragmentManager.beginTransaction().replace(R.id.fragment_content, SettingsFragment()).commit()

        disposables.add(
                appThemeHolder
                        .observeTheme()
                        .subscribe { appTheme ->
                            if (currentAppTheme !== appTheme) {
                                currentAppTheme = appTheme
                                recreate()
                            }
                        }
        )
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home)
            finish()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

    companion object {

        fun getIntent(context: Context): Intent {
            return Intent(context, SettingsActivity::class.java)
        }
    }
}
