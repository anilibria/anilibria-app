package ru.radiationx.anilibria.ui.activities

import android.os.Build
import android.os.Bundle
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.synthetic.main.activity_country_blocked.*
import ru.radiationx.anilibria.BuildConfig
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.data.datasource.remote.Api
import ru.radiationx.data.system.LocaleHolder
import ru.radiationx.shared_app.di.injectDependencies

class CountryBlockedActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies()
        setTheme(R.style.DayNightAppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            resources.configuration.locales[0]
        } else {
            resources.configuration.locale
        }
        if (Api.STORE_APP_IDS.contains(BuildConfig.APPLICATION_ID) && LocaleHolder.checkAvail(locale.country)) {
            startActivity(Screens.Main().getActivityIntent(this))
            finish()
            return
        }

        setContentView(R.layout.activity_country_blocked)
        ImageLoader.getInstance().displayImage("assets://LibriaTyanDn.png", countryBlockedImage)
        countryBlockedExit.setOnClickListener { finish() }
    }
}