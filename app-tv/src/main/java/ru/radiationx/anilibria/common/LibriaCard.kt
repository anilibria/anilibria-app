package ru.radiationx.anilibria.common

import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.entity.domain.types.YoutubeId
import ru.radiationx.data.entity.domain.youtube.YoutubeItem

data class LibriaCard(
    val title: String,
    val description: String,
    val image: String,
    val type: Type
) {

    var rawData: Any? = null

    val releaseId: ReleaseId?
        get() = (rawData as? Release?)?.id

    val youtubeId: YoutubeId?
        get() = (rawData as? YoutubeItem?)?.id

    enum class Type {
        RELEASE,
        YOUTUBE
    }
}