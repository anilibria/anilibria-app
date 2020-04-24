package ru.radiationx.anilibria.di

import ru.radiationx.anilibria.screen.search.SearchController
import toothpick.config.Module

class SearchModule : Module() {

    init {
        bind(SearchController::class.java).singleton()
    }
}