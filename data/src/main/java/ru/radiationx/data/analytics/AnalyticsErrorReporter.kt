package ru.radiationx.data.analytics

interface AnalyticsErrorReporter {

    fun report(message: String, error: Throwable)

    fun report(group: String, message: String)

    fun report(group: String, message: String, error: Throwable)
}