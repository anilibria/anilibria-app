package ru.radiationx.anilibria.di

import ru.radiationx.anilibria.common.LibriaCardRouter
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.quill.QuillModule
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.Router

class NavigationModule : QuillModule() {

    init {
        val cicerone by lazy { Cicerone.create(GuidedRouter()) }
        instance<Router> { cicerone.router }
        instance { cicerone.router }
        instance { cicerone.navigatorHolder }
        single<LibriaCardRouter>()
    }
}