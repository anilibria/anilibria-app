package ru.radiationx.anilibria.screen.search.sort

import android.os.Bundle
import android.view.View
import androidx.leanback.widget.GuidedAction
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.fragment.FakeGuidedStepFragment
import ru.radiationx.data.entity.domain.search.SearchForm
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.putExtra
import ru.radiationx.shared.ktx.android.subscribeTo

class SearchSortGuidedFragment : FakeGuidedStepFragment() {

    companion object {
        private const val ARG_SORT = "arg sort"

        fun newInstance(sort: SearchForm.Sort? = null) = SearchSortGuidedFragment().putExtra {
            sort?.also { putString(ARG_SORT, it.name) }
        }
    }

    private val viewModel by viewModel<SearchSortViewModel>()

    override fun onProvideTheme(): Int = R.style.AppTheme_Player_LeanbackWizard

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(viewModel)
        arguments?.apply {
            viewModel.argSort =
                getString(ARG_SORT)?.let { SearchForm.Sort.valueOf(it) } ?: viewModel.argSort
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