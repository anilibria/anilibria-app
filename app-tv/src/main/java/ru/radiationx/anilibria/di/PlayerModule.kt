package ru.radiationx.anilibria.di

import ru.radiationx.anilibria.screen.player.PlayerController
import toothpick.config.Module

class PlayerModule : Module() {

    init {
        bind(PlayerController::class.java).singleton()
    }
}