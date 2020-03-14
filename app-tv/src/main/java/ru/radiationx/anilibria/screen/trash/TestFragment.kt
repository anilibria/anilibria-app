package ru.radiationx.anilibria.screen.trash

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.app.GuidedStepSupportFragment
import kotlinx.android.synthetic.main.test_fragment.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.fragment.DialogRouter
import ru.radiationx.anilibria.screen.DialogExampleFragment
import ru.radiationx.anilibria.screen.TestGuidedStepScreen
import ru.radiationx.anilibria.screen.TestScreen
import ru.radiationx.shared.ktx.android.attachBackPressed
import ru.radiationx.shared_app.screen.BaseFragment
import ru.terrakok.cicerone.Router
import javax.inject.Inject

class TestFragment : BaseFragment(R.layout.test_fragment), BrowseSupportFragment.MainFragmentAdapterProvider {

    private val selfMainFragmentAdapter by lazy { BrowseSupportFragment.MainFragmentAdapter(this) }

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var dialogRouter: DialogRouter

    override fun getMainFragmentAdapter(): BrowseSupportFragment.MainFragmentAdapter<*> {
        return selfMainFragmentAdapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        attachBackPressed {
            if (isEnabled) {
                Toast.makeText(requireContext(), "Hello", Toast.LENGTH_SHORT).show()
                isEnabled = false
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        testText.text = "${dependencyInjector.parentScopeTag} > ${dependencyInjector.screenScopeTag}"
        btnback.setOnClickListener {
            router.exit()
        }
        btnfwd.setOnClickListener {
            router.navigateTo(TestScreen())
        }

        btndialog.setOnClickListener {
            dialogRouter.navigateTo(TestGuidedStepScreen())
        }
    }
}