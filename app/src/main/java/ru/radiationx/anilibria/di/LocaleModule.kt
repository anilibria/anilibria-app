package ru.radiationx.anilibria.di

import ru.radiationx.data.system.LocaleHolder
import toothpick.config.Module
import java.util.*

class LocaleModule(locale: Locale) : Module() {

    init {
        bind(LocaleHolder::class.java).toInstance(LocaleHolder(locale))
    }
}