package ru.radiationx.anilibria.ui.activities.player.models

import ru.radiationx.data.common.ReleaseId

data class PlayerDataState(
    val id: ReleaseId,
    val title: String,
    val episodeTitle: String,
)