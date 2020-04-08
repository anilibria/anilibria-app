package ru.radiationx.anilibria.screen.player

data class Video(
    val url: String,
    val seek: Long,
    val title: String,
    val subtitle: String
)