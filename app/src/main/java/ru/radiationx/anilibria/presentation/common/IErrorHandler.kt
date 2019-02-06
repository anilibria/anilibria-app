package ru.radiationx.anilibria.presentation.common

/**
 * Created by radiationx on 23.02.18.
 */
interface IErrorHandler {
    fun handle(throwable: Throwable, messageListener: ((Throwable, String?) -> Unit)? = null)
}