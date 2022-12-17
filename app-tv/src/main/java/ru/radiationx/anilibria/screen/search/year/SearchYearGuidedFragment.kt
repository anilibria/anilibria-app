package ru.radiationx.anilibria.screen.search.year

import ru.radiationx.anilibria.screen.search.BaseSearchValuesGuidedFragment
import ru.radiationx.anilibria.screen.search.BaseSearchValuesViewModel
import ru.radiationx.quill.viewModel

class SearchYearGuidedFragment : BaseSearchValuesGuidedFragment() {

    override val viewModel: BaseSearchValuesViewModel by viewModel<SearchYearViewModel> {
        argExtra
    }
}