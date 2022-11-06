package ru.radiationx.anilibria.di

import ru.radiationx.anilibria.common.LibriaCardRouter
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import toothpick.config.Module

class NavigationModule : Module() {

    init {
        val cicerone = Cicerone.create(GuidedRouter())
        bind(Router::class.java).toInstance(cicerone.router)
        bind(GuidedRouter::class.java).toInstance(cicerone.router)
        bind(NavigatorHolder::class.java).toInstance(cicerone.navigatorHolder)
        bind(LibriaCardRouter::class.java).singleton()
    }
}