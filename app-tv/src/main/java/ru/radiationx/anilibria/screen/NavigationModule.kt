package ru.radiationx.anilibria.screen

import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.Router
import toothpick.config.Module

class NavigationModule : Module() {
    init {
        val cicerone = Cicerone.create()
        bind(Router::class.java).toInstance(cicerone.router)
        bind(NavigatorHolder::class.java).toInstance(cicerone.getNavigatorHolder())
    }
}