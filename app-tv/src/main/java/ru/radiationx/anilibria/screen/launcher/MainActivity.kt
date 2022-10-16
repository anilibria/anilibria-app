package ru.radiationx.anilibria.screen.launcher

import android.content.Intent
import android.os.Bundle
import android.util.Log
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.GradientBackgroundManager
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.common.fragment.GuidedStepNavigator
import ru.radiationx.anilibria.contentprovider.suggestions.SuggestionsContentProvider
import ru.radiationx.anilibria.di.*
import ru.radiationx.shared.ktx.android.subscribeTo
import ru.radiationx.shared_app.di.viewModel
import ru.radiationx.shared_app.screen.ScopedFragmentActivity
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
    lateinit var guidedRouter: GuidedRouter

    @Inject
    lateinit var navigatorHolder: NavigatorHolder

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        dependencyInjector.installModules(
            ActivityModule(this),
            NavigationModule(),
            PlayerModule(),
            UpdateModule(),
            SearchModule(),
            module {
                bind(GradientBackgroundManager::class.java).toInstance(GradientBackgroundManager(this@MainActivity))
            }
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragments)
        lifecycle.addObserver(viewModel)

        subscribeTo(viewModel.appReadyAction) {
            handleIntent(intent)
        }

        if (savedInstanceState == null) {
            viewModel.coldLaunch()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        navigatorHolder.setNavigator(navigator)
    }

    override fun onPause() {
        navigatorHolder.removeNavigator()
        super.onPause()
    }

    private fun handleIntent(intent: Intent?) {
        intent ?: return
        if (intent.action == SuggestionsContentProvider.INTENT_ACTION) {
            val uri = intent.data ?: return
            val id = uri.lastPathSegment?.toInt() ?: return
            viewModel.openRelease(id)
        }
    }
}
