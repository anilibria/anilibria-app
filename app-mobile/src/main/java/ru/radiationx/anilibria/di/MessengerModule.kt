package ru.radiationx.anilibria.di

import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.ui.common.ErrorHandler
import ru.radiationx.anilibria.utils.messages.SystemMessenger
import ru.radiationx.quill.QuillModule

class MessengerModule : QuillModule() {

    init {
        single<SystemMessenger>()
        singleImpl<IErrorHandler, ErrorHandler>()
    }

}