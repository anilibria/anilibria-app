package ru.radiationx.anilibria.presentation.common

import ru.radiationx.anilibria.navigation.BaseFragmentScreen
import com.github.terrakok.cicerone.Router

/**
 * Created by radiationx on 03.02.18.
 */
interface ILinkHandler {
    fun handle(url: String, router: Router?, doNavigate: Boolean = true): Boolean
    fun findScreen(url: String): BaseFragmentScreen?
}