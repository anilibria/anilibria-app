package ru.radiationx.anilibria.ui.common

import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.Router

interface RouterProvider {
    fun getRouter(): Router
    fun getNavigator(): Navigator
}
