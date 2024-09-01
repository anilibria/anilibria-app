package ru.radiationx.anilibria.screen.launcher

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.github.terrakok.cicerone.NavigatorHolder
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.fragment.GuidedStepNavigator
import ru.radiationx.anilibria.contentprovider.suggestions.SuggestionsContentProvider
import ru.radiationx.anilibria.di.ActivityModule
import ru.radiationx.anilibria.di.NavigationModule
import ru.radiationx.anilibria.di.PlayerModule
import ru.radiationx.anilibria.di.SearchModule
import ru.radiationx.anilibria.di.UpdateModule
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.quill.inject
import ru.radiationx.quill.installModules
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.subscribeTo

class MainActivity : FragmentActivity() {

    private val viewModel: AppLauncherViewModel by viewModel()

    private val navigator by lazy {
        GuidedStepNavigator(
            this,
            R.id.fragmentContainer
        )
    }

    private val navigatorHolder by inject<NavigatorHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        installModules(
            ActivityModule(this),
            NavigationModule(),
            PlayerModule(),
            UpdateModule(),
            SearchModule(),
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragments)
        lifecycle.addObserver(viewModel)

        subscribeTo(viewModel.appReadyState) {
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
