package ru.radiationx.anilibria.di

import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.Router
import ru.radiationx.anilibria.common.LibriaCardRouter
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.quill.QuillModule

class NavigationModule : QuillModule() {

    init {
        val cicerone by lazy { Cicerone.create(GuidedRouter()) }
        instance<Router> { cicerone.router }
        instance { cicerone.router }
        instance { cicerone.getNavigatorHolder() }
        single<LibriaCardRouter>()
    }
}