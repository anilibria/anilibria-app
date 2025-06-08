package ru.radiationx.anilibria.ui.fragments.search.di

import ru.radiationx.anilibria.ui.fragments.search.controller.SearchController
import ru.radiationx.quill.QuillModule

class SearchModule : QuillModule() {
    init {
        single<SearchController>()
    }
}