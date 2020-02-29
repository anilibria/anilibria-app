package ru.radiationx.anilibria

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.transition.TransitionManager
import kotlinx.android.synthetic.main.fragment_config.*
import ru.radiationx.anilibria.screen.launcher.AppLauncherViewModel
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.shared.ktx.android.gone
import ru.radiationx.shared.ktx.android.visible
import ru.radiationx.shared_app.BaseFragmentActivity
import ru.radiationx.shared_app.ScopedAppNavigator
import ru.radiationx.shared_app.viewModel
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor
import toothpick.ktp.delegate.inject
import javax.inject.Inject

class MainActivity : BaseFragmentActivity() {

    private val viewModel: AppLauncherViewModel by viewModel()

    private val navigator by lazy { ScopedAppNavigator(this, R.id.fragmentContainer, scopeProvider = this) }

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
