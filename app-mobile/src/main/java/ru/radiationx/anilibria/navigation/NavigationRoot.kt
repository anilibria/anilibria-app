package ru.radiationx.anilibria.navigation

import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.Router
import javax.inject.Inject

/* Cicerone навигация
* root - для активити
* local - для табов, типа как в семпле cicerone
* */
class NavigationRoot @Inject constructor() {
    private val cicerone: Cicerone<Router> = Cicerone.create(Router())

    val router: Router = cicerone.router
    val holder: NavigatorHolder = cicerone.getNavigatorHolder()
}