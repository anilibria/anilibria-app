package ru.radiationx.anilibria.common

data class LibriaCard(
    val id: Int,
    val title: String,
    val description: String,
    val image: String,
    val type: Type
) {

    var rawData: Any? = null

    enum class Type {
        RELEASE,
        YOUTUBE
    }
}