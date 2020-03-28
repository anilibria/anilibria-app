package ru.radiationx.anilibria.screen.player

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.commitNow
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.GradientBackgroundManager
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.common.fragment.GuidedStepNavigator
import ru.radiationx.anilibria.di.ActivityModule
import ru.radiationx.anilibria.di.NavigationModule
import ru.radiationx.shared_app.di.putScopeArgument
import ru.radiationx.shared_app.screen.ScopedFragmentActivity
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import toothpick.ktp.binding.module
import javax.inject.Inject

class PlayerActivity : ScopedFragmentActivity(R.layout.activity_fragments) {

    companion object {
        private const val ARG_ID = "id"

        fun getIntent(context: Context, releaseId: Int): Intent = Intent(context, PlayerActivity::class.java).apply {
            putExtra(ARG_ID, releaseId)
        }
    }

    private val navigator by lazy {
        GuidedStepNavigator(
            this,
            R.id.fragmentContainer,
            scopeProvider = this
        )
    }

    @Inject
    lateinit var navigatorHolder: NavigatorHolder

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var guidedRouter: GuidedRouter

    override fun onCreate(savedInstanceState: Bundle?) {
        dependencyInjector.installModules(
            ActivityModule(this),
            NavigationModule()
        )

        super.onCreate(savedInstanceState)

        val releaseId = intent?.getIntExtra(ARG_ID, -1) ?: -1

        if (savedInstanceState == null) {
            supportFragmentManager.commitNow {
                val fragment = PlayerFragment
                    .newInstance(releaseId)
                    .putScopeArgument(screenScopeTag)
                replace(R.id.fragmentContainer, fragment)
            }
        }
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