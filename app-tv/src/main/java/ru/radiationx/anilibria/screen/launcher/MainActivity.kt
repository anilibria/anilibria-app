package ru.radiationx.anilibria.screen.launcher

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.common.fragment.GuidedStepNavigator
import ru.radiationx.anilibria.contentprovider.suggestions.SuggestionsContentProvider
import ru.radiationx.anilibria.di.*
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.quill.installQuillModules
import ru.radiationx.quill.quillInject
import ru.radiationx.quill.quillViewModel
import ru.radiationx.shared.ktx.android.subscribeTo
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router

class MainActivity : FragmentActivity() {

    private val viewModel: AppLauncherViewModel by quillViewModel()

    private val navigator by lazy {
        GuidedStepNavigator(
            this,
            R.id.fragmentContainer
        )
    }

    private val router by quillInject<Router>()

    private val guidedRouter by quillInject<GuidedRouter>()

    private val navigatorHolder by quillInject<NavigatorHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        installQuillModules(
            ActivityModule(this),
            NavigationModule(),
            PlayerModule(),
            UpdateModule(),
            SearchModule(),
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
            viewModel.openRelease(ReleaseId(id))
        }
    }
}
