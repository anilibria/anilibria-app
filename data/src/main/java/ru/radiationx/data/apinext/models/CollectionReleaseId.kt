package ru.radiationx.data.apinext.models

import ru.radiationx.data.apinext.models.enums.CollectionType
import ru.radiationx.data.entity.domain.types.ReleaseId

data class CollectionReleaseId(
    val id: ReleaseId,
    val type: CollectionType
)
