package ru.radiationx.anilibria.ui.activities.player.models

import ru.radiationx.data.entity.domain.release.Episode
import ru.radiationx.data.entity.domain.types.ReleaseId

data class PlayerRelease(
    val id: ReleaseId,
    val name: String,
    val episodes: List<Episode>,
)
