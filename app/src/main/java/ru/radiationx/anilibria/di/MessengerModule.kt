package ru.radiationx.anilibria.di

import ru.radiationx.anilibria.model.system.messages.SystemMessenger
import toothpick.config.Module

class MessengerModule : Module() {

    init {
        bind(SystemMessenger::class.java).singletonInScope()
    }

}