package ru.radiationx.anilibria.ui.common

/**
 * Created by radiationx on 23.02.18.
 */
interface IntentHandler {
    fun handle(url: String): Boolean
}