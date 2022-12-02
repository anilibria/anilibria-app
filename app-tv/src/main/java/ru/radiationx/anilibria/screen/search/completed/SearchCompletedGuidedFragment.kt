package ru.radiationx.anilibria.screen.search.completed

import android.os.Bundle
import android.view.View
import androidx.leanback.widget.GuidedAction
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.fragment.FakeGuidedStepFragment
import ru.radiationx.quill.quillViewModel
import ru.radiationx.shared.ktx.android.putExtra
import ru.radiationx.shared.ktx.android.subscribeTo

class SearchCompletedGuidedFragment : FakeGuidedStepFragment() {

    companion object {
        private const val ARG_COMPLETED = "arg completed"

        fun newInstance(onlyCompleted: Boolean) = SearchCompletedGuidedFragment().putExtra {
            putBoolean(ARG_COMPLETED, onlyCompleted)
        }
    }

    private val viewModel by quillViewModel<SearchCompletedViewModel>()

    override fun onProvideTheme(): Int = R.style.AppTheme_Player_LeanbackWizard

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(viewModel)
        arguments?.apply {
            viewModel.argCompleted = getBoolean(ARG_COMPLETED, viewModel.argCompleted)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeTo(viewModel.titlesData) {
            actions = it.mapIndexed { index: Int, title: String ->
                GuidedAction.Builder(requireContext())
                    .id(index.toLong())
                    .title(title)
                    .build()
            }
        }

        subscribeTo(viewModel.selectedIndex) {
            selectedActionPosition = it
        }
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        viewModel.applySort(action.id.toInt())
    }
}