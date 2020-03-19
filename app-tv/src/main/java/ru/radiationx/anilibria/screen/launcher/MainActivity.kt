package ru.radiationx.anilibria.screen.launcher

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.GradientBackgroundManager
import ru.radiationx.anilibria.common.fragment.DialogRouter
import ru.radiationx.anilibria.common.fragment.GuidedStepNavigator
import ru.radiationx.shared_app.screen.ScopedFragmentActivity
import ru.radiationx.shared_app.di.viewModel
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import toothpick.ktp.binding.module
import javax.inject.Inject

class MainActivity : ScopedFragmentActivity() {

    private val viewModel: AppLauncherViewModel by viewModel()

    private val navigator by lazy {
        GuidedStepNavigator(
            this,
            R.id.fragmentContainer,
            scopeProvider = this
        )
    }

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var dialogRouter: DialogRouter

    @Inject
    lateinit var navigatorHolder: NavigatorHolder

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        dependencyInjector.installModules(module {
            bind(FragmentActivity::class.java).toInstance(this@MainActivity)
            bind(GradientBackgroundManager::class.java).toInstance(GradientBackgroundManager(this@MainActivity))
        })
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        lifecycle.addObserver(viewModel)

        if (savedInstanceState == null) {
            viewModel.coldLaunch()
        }
        /* supportFragmentManager
             .beginTransaction()
             .add(R.id.fragmentContainer, DialogExampleFragment())
             .commit()*/
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        navigatorHolder.setNavigator(navigator)
    }

    override fun onPause() {
        navigatorHolder.removeNavigator()
        super.onPause()
    }

    override fun onBackPressed() {
        /*val currentFragment = GuidedStepSupportFragment.getCurrentGuidedStepSupportFragment(supportFragmentManager)
        if (currentFragment != null) {
            dialogRouter.exit()
        }else{
        }*/
        super.onBackPressed()
    }
}
