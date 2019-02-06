package ru.radiationx.anilibria.presentation.common

import ru.radiationx.anilibria.navigation.BaseAppScreen
import ru.terrakok.cicerone.Router

/**
 * Created by radiationx on 03.02.18.
 */
interface LinkHandler {
    fun handle(url: String, router: Router?, doNavigate: Boolean = true): Boolean
    fun findScreen(url: String): BaseAppScreen?
}