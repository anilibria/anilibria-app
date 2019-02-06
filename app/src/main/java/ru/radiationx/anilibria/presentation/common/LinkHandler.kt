package ru.radiationx.anilibria.presentation.common

import ru.radiationx.anilibria.navigation.AppRouter
import ru.radiationx.anilibria.navigation.BaseAppScreen

/**
 * Created by radiationx on 03.02.18.
 */
interface LinkHandler {
    fun handle(url: String, router: AppRouter?, doNavigate: Boolean = true): Boolean
    fun findScreen(url: String): BaseAppScreen?
}