package ru.radiationx.anilibria.screen.player

import android.content.Context
import androidx.leanback.widget.Action
import ru.radiationx.anilibria.R
import ru.radiationx.shared.ktx.android.getCompatDrawable

class SpeedAction(context: Context) : Action(R.id.player_action_quality.toLong()) {

    init {
        icon = context.getCompatDrawable(R.drawable.ic_play_speed)
        label1 = "Скорость воспроизведения"
    }
}