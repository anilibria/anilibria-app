package ru.radiationx.anilibria.ui.common

import ru.terrakok.cicerone.Navigator
import ru.radiationx.anilibria.navigation.AppRouter

interface RouterProvider {
    fun getRouter(): AppRouter
    fun getNavigator(): Navigator
}
