package ru.radiationx.anilibria.screen.launcher

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.github.terrakok.cicerone.NavigatorHolder
import dev.androidbroadcast.vbpd.viewBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.fragment.GuidedStepNavigator
import ru.radiationx.anilibria.contentprovider.suggestions.SuggestionsContentProvider
import ru.radiationx.anilibria.databinding.ActivityFragmentsBinding
import ru.radiationx.anilibria.di.ActivityModule
import ru.radiationx.anilibria.di.NavigationModule
import ru.radiationx.anilibria.di.PlayerModule
import ru.radiationx.anilibria.di.SearchModule
import ru.radiationx.anilibria.di.UpdateModule
import ru.radiationx.data.common.ReleaseId
import ru.radiationx.quill.inject
import ru.radiationx.quill.installModules
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.launchInResumed
import ru.radiationx.shared.ktx.android.setBackgroundTintRes
import ru.radiationx.shared.ktx.android.subscribeTo
import ru.radiationx.shared_app.networkstatus.NetworkStatusState
import ru.radiationx.shared_app.networkstatus.NetworkStatusViewModel
import ru.radiationx.shared_app.networkstatus.toViewState

class MainActivity : FragmentActivity(R.layout.activity_fragments) {

    private val viewModel: AppLauncherViewModel by viewModel()
    private val networkStatusViewModel: NetworkStatusViewModel by viewModel()

    private val binding by viewBinding<ActivityFragmentsBinding>()

    private val navigator by lazy {
        GuidedStepNavigator(
            activity = this,
            containerId = R.id.fragmentContainer,
            guidedContainerId = R.id.guidedFragmentContainer
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
        lifecycle.addObserver(viewModel)

        subscribeTo(viewModel.appReadyState) {
            handleIntent(intent)
        }

        networkStatusViewModel.state.onEach {
            delay(2000)
            bindStatus(binding.root, binding.networkStatusWrapper, binding.networkStatus, it)
        }.launchIn(lifecycleScope)

        if (savedInstanceState == null) {
            viewModel.coldLaunch()
        }
    }

    override fun onNewIntent(intent: Intent) {
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

    private suspend fun bindStatus(
        transitionRoot: ViewGroup,
        statusWrapper: ViewGroup,
        statusView: TextView,
        state: NetworkStatusState
    ) {
        val viewState = state.toViewState()
        statusView.text = viewState.text
        statusView.setBackgroundTintRes(viewState.colorRes)
        if (!viewState.isVisible) {
            delay(500)
        }
        updateVisibilityError(transitionRoot, statusWrapper, viewState.isVisible)
    }

    private fun updateVisibilityError(
        transitionRoot: ViewGroup,
        statusWrapper: ViewGroup,
        visible: Boolean
    ) {
        TransitionManager.beginDelayedTransition(
            transitionRoot,
            AutoTransition().apply {
                addTarget(statusWrapper)
            }
        )
        statusWrapper.isVisible = visible
    }
}
