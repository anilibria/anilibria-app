package ru.radiationx.anilibria.presentation

import ru.terrakok.cicerone.Router

/**
 * Created by radiationx on 03.02.18.
 */
interface LinkHandler {
    fun handle(url: String, router: Router): Boolean
}