package ru.radiationx.anilibria.screen.mainpages

import androidx.leanback.app.BrowseSupportFragment
import ru.radiationx.anilibria.R
import ru.radiationx.shared_app.screen.BaseFragment

class EmptyFragment : BaseFragment(R.layout.fragment_empty), BrowseSupportFragment.MainFragmentAdapterProvider {

    private val selfMainFragmentAdapter by lazy { BrowseSupportFragment.MainFragmentAdapter(this) }

    override fun getMainFragmentAdapter(): BrowseSupportFragment.MainFragmentAdapter<*> {
        return selfMainFragmentAdapter
    }

}