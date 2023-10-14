package ru.radiationx.anilibria.di

import ru.radiationx.data.system.LocaleHolder
import ru.radiationx.quill.QuillModule
import java.util.Locale

class LocaleModule(locale: Locale) : QuillModule() {

    init {
        instance {
            LocaleHolder()
        }
    }
}