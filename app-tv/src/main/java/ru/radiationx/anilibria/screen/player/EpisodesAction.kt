package ru.radiationx.anilibria.screen.player

import android.content.Context
import androidx.leanback.widget.Action
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.extension.getCompatDrawable

class EpisodesAction(context: Context) : Action(R.id.player_action_episodes.toLong()) {

    init {
        icon = context.getCompatDrawable(R.drawable.ic_playlist_play_black_24dp)
        label1 = "Выбрать серию"
    }
}