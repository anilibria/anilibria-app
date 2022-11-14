package ru.radiationx.anilibria.ui.fragments.comments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import kotlinx.android.synthetic.main.fragment_lazy.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.common.ScopeProvider
import ru.radiationx.anilibria.ui.fragments.ScopeFragment
import ru.radiationx.shared.ktx.android.putExtra
import ru.radiationx.shared_app.di.DI

class LazyVkCommentsFragment : Fragment(R.layout.fragment_lazy), ScopeProvider {

    override val screenScope: String by lazy {
        arguments?.getString(ScopeFragment.ARG_SCREEN_SCOPE, null) ?: DI.DEFAULT_SCOPE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            addFragmentIfReady()
        }
    }

    override fun onResume() {
        super.onResume()
        addFragmentIfReady()
    }

    private fun addFragmentIfReady() {
        val isReady = isVisible && isResumed
        if (!isReady) {
            return
        }
        val isAlreadyContains = childFragmentManager.findFragmentById(R.id.lazyContainer) != null

        if (isAlreadyContains) {
            lazyProgress.isVisible = false
            return
        }

        val fragment = VkCommentsFragment().also {
            val newBundle = (this.arguments?.clone() as Bundle?)
            it.arguments = newBundle
            it.putExtra {
                putString(ScopeFragment.ARG_SCREEN_SCOPE, screenScope)
            }
        }
        childFragmentManager.commit {
            replace(R.id.lazyContainer, fragment)
        }
        lazyProgress.isVisible = false
    }

}