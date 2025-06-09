package ru.radiationx.anilibria.ui.common.release

import ru.radiationx.data.api.collections.models.CollectionType

data class ContextReleaseState(
    val title: String = "",
    val hasAuth: Boolean = false,
    val isInFavorite: Boolean = false,
    val collectionType: CollectionType? = null,
    val collections: Set<CollectionType> = emptySet(),
    val isInHistory: Boolean = false
)
