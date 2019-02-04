package ru.radiationx.anilibria.presentation

import ru.radiationx.anilibria.Screens
import ru.radiationx.anilibria.ui.navigation.AppRouter

/**
 * Created by radiationx on 03.02.18.
 */
interface LinkHandler {
    fun handle(url: String, router: AppRouter?, doNavigate: Boolean = true): Boolean
    fun findScreen(url: String): Screens.AppScreen?
}