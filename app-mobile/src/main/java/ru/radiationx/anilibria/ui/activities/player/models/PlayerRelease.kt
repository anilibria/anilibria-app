package ru.radiationx.anilibria.ui.activities.player.models

import ru.radiationx.data.api.releases.models.Episode
import ru.radiationx.data.common.ReleaseId

data class PlayerRelease(
    val id: ReleaseId,
    val name: String,
    val episodes: List<Episode>,
)
