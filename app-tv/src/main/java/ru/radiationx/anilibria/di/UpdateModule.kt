package ru.radiationx.anilibria.di

import ru.radiationx.anilibria.screen.update.UpdateController
import toothpick.config.Module

class UpdateModule : Module() {

    init {
        bind(UpdateController::class.java).singleton()
    }
}