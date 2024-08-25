package ru.radiationx.data.apinext

import ru.radiationx.data.apinext.models.enums.CollectionType

fun String.toCollectionType(): CollectionType {
    return when (this) {
        "PLANNED" -> CollectionType.Planned
        "WATCHED" -> CollectionType.Watched
        "WATCHING" -> CollectionType.Watching
        "POSTPONED" -> CollectionType.Postponed
        "ABANDONED" -> CollectionType.Abandoned
        else -> CollectionType.Unknown(this)
    }
}

fun CollectionType.toListQuery(): String {
    return when (this) {
        CollectionType.Planned -> "PLANNED"
        CollectionType.Watched -> "WATCHED"
        CollectionType.Watching -> "WATCHING"
        CollectionType.Postponed -> "POSTPONED"
        CollectionType.Abandoned -> "ABANDONED"
        is CollectionType.Unknown -> raw
    }
}