package ru.radiationx.anilibria.screen.search.completed

import android.os.Bundle
import android.view.View
import androidx.leanback.widget.GuidedAction
import kotlinx.coroutines.flow.filterNotNull
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.fragment.FakeGuidedStepFragment
import ru.radiationx.quill.QuillExtra
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.getExtraNotNull
import ru.radiationx.shared.ktx.android.putExtra
import ru.radiationx.shared.ktx.android.subscribeTo

data class SearchCompletedExtra(
    val isCompleted: Boolean
) : QuillExtra

class SearchCompletedGuidedFragment : FakeGuidedStepFragment() {

    companion object {
        private const val ARG_COMPLETED = "arg completed"

        fun newInstance(onlyCompleted: Boolean) = SearchCompletedGuidedFragment().putExtra {
            putBoolean(ARG_COMPLETED, onlyCompleted)
        }
    }

    private val viewModel by viewModel<SearchCompletedViewModel> {
        SearchCompletedExtra(getExtraNotNull(ARG_COMPLETED))
    }

    override fun onProvideTheme(): Int = R.style.AppTheme_Player_LeanbackWizard

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycle.addObserver(viewModel)

        subscribeTo(viewModel.titlesData) {
            actions = it.mapIndexed { index: Int, title: String ->
                GuidedAction.Builder(requireContext())
                    .id(index.toLong())
                    .title(title)
                    .build()
            }
        }

        subscribeTo(viewModel.selectedIndex.filterNotNull()) {
            selectedActionPosition = it
        }
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        viewModel.applySort(action.id.toInt())
    }
}