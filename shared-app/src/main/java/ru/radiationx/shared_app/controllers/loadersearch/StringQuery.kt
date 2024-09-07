package ru.radiationx.shared_app.controllers.loadersearch

data class StringQuery(
    val value: String
) : SearchQuery {

    override fun isEmpty(): Boolean {
        return value.isEmpty()
    }
}