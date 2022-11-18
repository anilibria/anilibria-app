package ru.radiationx.anilibria.common

import ru.radiationx.data.entity.domain.types.ReleaseId

data class LibriaCard(
    val title: String,
    val description: String,
    val image: String,
    val type: Type
) {

    sealed class Type {
        data class Release(val releaseId: ReleaseId) : Type()
        data class Youtube(val link: String) : Type()
    }
}