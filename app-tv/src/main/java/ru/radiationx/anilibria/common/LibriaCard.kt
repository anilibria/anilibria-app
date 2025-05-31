package ru.radiationx.anilibria.common

import ru.radiationx.data.entity.common.Url
import ru.radiationx.data.entity.domain.types.ReleaseId

data class LibriaCard(
    val title: String,
    val description: String,
    val image: Url.Relative?,
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