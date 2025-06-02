package ru.radiationx.anilibria.common

import ru.radiationx.data.common.ReleaseId
import ru.radiationx.data.common.Url

data class LibriaCard(
    val title: String,
    val description: String,
    val image: Url.Path?,
    val type: Type
) : CardItem {

    override fun getId(): Int {
        return type.hashCode()
    }

    sealed class Type {
        data class Release(val releaseId: ReleaseId) : Type()
        data class Youtube(val link: Url) : Type()
    }
}