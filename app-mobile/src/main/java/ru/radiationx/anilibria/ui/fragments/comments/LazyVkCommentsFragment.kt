package ru.radiationx.anilibria.ui.fragments.comments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.FragmentLazyBinding
import ru.radiationx.anilibria.ui.fragments.BaseDimensionsFragment

class LazyVkCommentsFragment : BaseDimensionsFragment(R.layout.fragment_lazy) {

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

        val fragment = VkCommentsFragment().also {
            val newBundle = (this.arguments?.clone() as Bundle?)
            it.arguments = newBundle
        }
        childFragmentManager.commit {
            replace(R.id.lazyContainer, fragment)
        }
        binding.lazyProgress.isVisible = false
    }

}