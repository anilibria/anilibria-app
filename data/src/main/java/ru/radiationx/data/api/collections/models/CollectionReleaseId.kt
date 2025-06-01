package ru.radiationx.data.api.collections.models

import ru.radiationx.data.common.ReleaseId

data class CollectionReleaseId(
    val id: ReleaseId,
    val type: CollectionType
)
