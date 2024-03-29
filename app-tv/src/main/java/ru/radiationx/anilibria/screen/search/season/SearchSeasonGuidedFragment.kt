package ru.radiationx.anilibria.screen.search.season

import ru.radiationx.anilibria.screen.search.BaseSearchValuesGuidedFragment
import ru.radiationx.anilibria.screen.search.BaseSearchValuesViewModel
import ru.radiationx.quill.viewModel

class SearchSeasonGuidedFragment : BaseSearchValuesGuidedFragment() {

    override val viewModel: BaseSearchValuesViewModel by viewModel<SearchSeasonViewModel> {
        argExtra
    }
}