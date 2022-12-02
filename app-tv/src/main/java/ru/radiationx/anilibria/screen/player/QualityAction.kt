package ru.radiationx.anilibria.screen.player

import android.content.Context
import androidx.leanback.widget.PlaybackControlsRow
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.extension.getCompatDrawable

class QualityAction(context: Context) :
    PlaybackControlsRow.MultiAction(R.id.player_action_quality) {

    companion object {
        const val INDEX_SD = 0
        const val INDEX_HD = 1
        const val INDEX_FHD = 2
    }

    init {
        setDrawables(
            arrayOf(
                context.getCompatDrawable(R.drawable.ic_quality_sd_base),
                context.getCompatDrawable(R.drawable.ic_quality_hd_base),
                context.getCompatDrawable(R.drawable.ic_quality_full_hd_base)
            )
        )
        setLabels(
            arrayOf(
                "480p",
                "720p",
                "1080p"
            )
        )
    }


}