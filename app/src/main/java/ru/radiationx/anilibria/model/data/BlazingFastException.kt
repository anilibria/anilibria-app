package ru.radiationx.anilibria.model.data

class BlazingFastException(
        val content: String,
        val url: String
) : Exception("BlazingFast")