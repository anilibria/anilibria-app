package ru.radiationx.anilibria.di

import ru.radiationx.anilibria.screen.catalog.CatalogController
import toothpick.config.Module

class CatalogModule : Module() {

    init {
        bind(CatalogController::class.java).singleton()
    }
}