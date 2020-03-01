package ru.radiationx.anilibria.screen.launcher

import android.os.Bundle
import ru.radiationx.anilibria.R
import ru.radiationx.shared_app.screen.BaseFragmentActivity
import ru.radiationx.shared_app.navigation.ScopedAppNavigator
import ru.radiationx.shared_app.di.viewModel
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import javax.inject.Inject

class MainActivity : BaseFragmentActivity() {

    private val viewModel: AppLauncherViewModel by viewModel()

    private val navigator by lazy {
        ScopedAppNavigator(
            this,
            R.id.fragmentContainer,
            scopeProvider = this
        )
    }

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var navigatorHolder: NavigatorHolder

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        lifecycle.addObserver(viewModel)
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        navigatorHolder.setNavigator(navigator)
    }

    override fun onPause() {
        navigatorHolder.removeNavigator()
        super.onPause()
    }
}
