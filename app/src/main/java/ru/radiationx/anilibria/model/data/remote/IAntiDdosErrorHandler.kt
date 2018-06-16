package ru.radiationx.anilibria.model.data.remote

interface IAntiDdosErrorHandler {
    fun handle(throwable: Throwable)
}