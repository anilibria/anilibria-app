package ru.radiationx.anilibria.presentation

/**
 * Created by radiationx on 23.02.18.
 */
interface ErrorHandler {
    fun handle(throwable: Throwable, messageListener: ((Throwable, String?) -> Unit)? = null)
}