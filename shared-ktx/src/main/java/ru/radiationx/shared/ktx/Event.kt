package ru.radiationx.shared.ktx

class Event<T>(private val content: T) {

    private var handled = false

    fun content() = if (handled) {
        null
    } else {
        handled = true
        content
    }
}