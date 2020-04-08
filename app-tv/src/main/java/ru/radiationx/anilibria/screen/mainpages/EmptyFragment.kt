package ru.radiationx.anilibria.screen.mainpages

import android.os.Bundle
import android.view.View
import androidx.leanback.app.BackgroundManager
import androidx.leanback.app.BrowseSupportFragment
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.GradientBackgroundManager
import ru.radiationx.shared_app.screen.ScopedFragment
import javax.inject.Inject

class EmptyFragment : ScopedFragment(R.layout.fragment_empty), BrowseSupportFragment.MainFragmentAdapterProvider {

    @Inject
    lateinit var backgroundManager: GradientBackgroundManager

    private val selfMainFragmentAdapter by lazy { BrowseSupportFragment.MainFragmentAdapter(this) }

    override fun getMainFragmentAdapter(): BrowseSupportFragment.MainFragmentAdapter<*> {
        return selfMainFragmentAdapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        backgroundManager.clearGradient()
    }

}