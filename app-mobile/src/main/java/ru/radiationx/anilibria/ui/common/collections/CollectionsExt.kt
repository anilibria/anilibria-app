package ru.radiationx.anilibria.ui.common.collections

import androidx.annotation.DrawableRes
import ru.radiationx.anilibria.R
import ru.radiationx.data.api.collections.models.CollectionType


fun CollectionType.toTitle(): String {
    return when (this) {
        CollectionType.Planned -> "Запланировано"
        CollectionType.Watching -> "Смотрю"
        CollectionType.Watched -> "Просмотрено"
        CollectionType.Postponed -> "Отложено"
        CollectionType.Abandoned -> "Брошено"
        is CollectionType.Unknown -> raw
    }
}

@DrawableRes
fun CollectionType.toIcRes(): Int {
    return when (this) {
        CollectionType.Planned -> R.drawable.ic_collection_planned
        CollectionType.Watching -> R.drawable.ic_collection_watching
        CollectionType.Watched -> R.drawable.ic_collection_watched
        CollectionType.Postponed -> R.drawable.ic_collection_postponed
        CollectionType.Abandoned -> R.drawable.ic_collection_abandoned
        is CollectionType.Unknown -> R.drawable.ic_collections
    }
}