package ru.radiationx.anilibria.di

import ru.radiationx.anilibria.screen.search.SearchController
import ru.radiationx.quill.QuillModule

class SearchModule : QuillModule() {

    init {
        single<SearchController>()
    }
}