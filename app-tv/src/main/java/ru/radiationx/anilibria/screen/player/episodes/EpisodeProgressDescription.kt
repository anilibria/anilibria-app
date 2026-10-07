package ru.radiationx.anilibria.screen.player.episodes

import ru.radiationx.data.entity.domain.release.EpisodeAccess
import ru.radiationx.shared.ktx.asTimeSecString
import java.util.Date

internal fun EpisodeAccess?.progressDescription(): String? =
    this?.takeIf { it.hasProgress }?.let {
        "Остановлена на ${Date(it.seek).asTimeSecString()}"
    }
