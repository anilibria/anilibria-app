package ru.radiationx.anilibria.ui.fragments.comments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.FragmentLazyBinding
import ru.radiationx.anilibria.ui.fragments.BaseDimensionsFragment
import ru.radiationx.anilibria.ui.fragments.TopScroller

class LazyVkCommentsFragment : BaseDimensionsFragment(R.layout.fragment_lazy), TopScroller {

    private val binding by viewBinding<FragmentLazyBinding>()

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

    override fun scrollToTop() {
        val fragment = childFragmentManager.findFragmentById(R.id.lazyContainer)
        if (fragment is TopScroller) {
            fragment.scrollToTop()
        }
    }

    private fun addFragmentIfReady() {
        val isReady = isVisible && isResumed
        if (!isReady) {
            return
        }
        val isAlreadyContains = childFragmentManager.findFragmentById(R.id.lazyContainer) != null

        if (isAlreadyContains) {
            binding.lazyProgress.isVisible = false
            return
        }

        val fragment = VkCommentsFragment()
        childFragmentManager.commit {
            replace(R.id.lazyContainer, fragment)
        }
        binding.lazyProgress.isVisible = false
    }

}