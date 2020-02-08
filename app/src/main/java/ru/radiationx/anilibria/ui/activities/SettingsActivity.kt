package ru.radiationx.anilibria.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import io.reactivex.disposables.CompositeDisposable
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.di.extensions.injectDependencies
import ru.radiationx.anilibria.extension.getPrefStyleRes
import ru.radiationx.anilibria.model.datasource.holders.AppThemeHolder
import ru.radiationx.anilibria.ui.fragments.settings.SettingsFragment
import javax.inject.Inject


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
        currentAppTheme = appThemeHolder.getTheme()
        setTheme(currentAppTheme.getPrefStyleRes())
        super.onCreate(savedInstanceState)
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
