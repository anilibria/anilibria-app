package ru.radiationx.anilibria.screen.trash

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.leanback.app.BrowseSupportFragment
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.databinding.TestFragmentBinding
import ru.radiationx.anilibria.screen.TestGuidedStepScreen
import ru.radiationx.anilibria.screen.TestScreen
import ru.radiationx.quill.getQuillScope
import ru.radiationx.quill.quillInject
import ru.radiationx.shared.ktx.android.attachBackPressed
import ru.terrakok.cicerone.Router

class TestFragment : Fragment(R.layout.test_fragment),
    BrowseSupportFragment.MainFragmentAdapterProvider {

    private val binding by viewBinding<TestFragmentBinding>()

    private val selfMainFragmentAdapter by lazy { BrowseSupportFragment.MainFragmentAdapter(this) }

    private val router by quillInject<Router>()

    private val guidedRouter by quillInject<GuidedRouter>()

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
        binding.testText.text =
            "${requireParentFragment().getQuillScope().name} > ${getQuillScope().name}"
        binding.btnback.setOnClickListener {
            router.exit()
        }
        binding.btnfwd.setOnClickListener {
            router.navigateTo(TestScreen())
        }

        binding.btndialog.setOnClickListener {
            guidedRouter.navigateTo(TestGuidedStepScreen())
        }
    }
}