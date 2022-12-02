package ru.radiationx.anilibria.screen.search.genre

import ru.radiationx.anilibria.screen.search.BaseSearchValuesGuidedFragment
import ru.radiationx.anilibria.screen.search.BaseSearchValuesViewModel
import ru.radiationx.quill.quillViewModel

class SearchGenreGuidedFragment : BaseSearchValuesGuidedFragment() {

    override val viewModel: BaseSearchValuesViewModel by quillViewModel<SearchGenreViewModel>()
}