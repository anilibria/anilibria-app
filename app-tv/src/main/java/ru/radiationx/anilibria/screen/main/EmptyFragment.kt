package ru.radiationx.anilibria.screen.main

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.leanback.app.BrowseSupportFragment
import kotlinx.android.synthetic.main.test_fragment.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.fragment.DialogRouter
import ru.radiationx.anilibria.screen.TestGuidedStepScreen
import ru.radiationx.anilibria.screen.TestScreen
import ru.radiationx.shared.ktx.android.attachBackPressed
import ru.radiationx.shared_app.screen.BaseFragment
import ru.terrakok.cicerone.Router
import javax.inject.Inject

class EmptyFragment : BaseFragment(R.layout.fragment_empty), BrowseSupportFragment.MainFragmentAdapterProvider {

    private val selfMainFragmentAdapter by lazy { BrowseSupportFragment.MainFragmentAdapter(this) }

    override fun getMainFragmentAdapter(): BrowseSupportFragment.MainFragmentAdapter<*> {
        return selfMainFragmentAdapter
    }

}