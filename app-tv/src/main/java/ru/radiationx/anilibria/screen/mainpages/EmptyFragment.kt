package ru.radiationx.anilibria.screen.mainpages

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.leanback.app.BrowseSupportFragment
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.GradientBackgroundManager

class EmptyFragment : Fragment(R.layout.fragment_empty),
    BrowseSupportFragment.MainFragmentAdapterProvider {

    private val backgroundManager by lazy { GradientBackgroundManager(requireActivity()) }

    private val selfMainFragmentAdapter by lazy { BrowseSupportFragment.MainFragmentAdapter(this) }

    override fun getMainFragmentAdapter(): BrowseSupportFragment.MainFragmentAdapter<*> {
        return selfMainFragmentAdapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        backgroundManager.clearGradient()
    }
}
