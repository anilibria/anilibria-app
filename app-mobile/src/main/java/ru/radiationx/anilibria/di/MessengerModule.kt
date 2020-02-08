package ru.radiationx.anilibria.di

import ru.radiationx.anilibria.utils.messages.SystemMessenger
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.ui.common.ErrorHandler
import toothpick.config.Module

class MessengerModule : Module() {

    init {
        bind(SystemMessenger::class.java).singletonInScope()
        bind(IErrorHandler::class.java).to(ErrorHandler::class.java).singletonInScope()
    }

}