package ru.radiationx.data.analytics

interface AnalyticsSender {

    fun send(key: String, vararg params: Pair<String, String>)

    fun error(groupId: String, message: String, throwable: Throwable)
}