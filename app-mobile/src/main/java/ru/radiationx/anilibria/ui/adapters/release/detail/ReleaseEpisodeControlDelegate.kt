package ru.radiationx.anilibria.ui.adapters.release.detail

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_release_episode_control.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.presentation.release.details.ReleaseEpisodesControlState
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseEpisodeControlItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate

/**
 * Created by radiationx on 13.01.18.
 */
class ReleaseEpisodeControlDelegate(
    private val itemListener: Listener
) : AppAdapterDelegate<ReleaseEpisodeControlItem, ListItem, ReleaseEpisodeControlDelegate.ViewHolder>(
    R.layout.item_release_episode_control,
    { it is ReleaseEpisodeControlItem },
    { ViewHolder(it, itemListener) }
) {

    override fun bindData(item: ReleaseEpisodeControlItem, holder: ViewHolder) =
        holder.bind(item.state, item.place)

    class ViewHolder(
        override val containerView: View,
        private val itemListener: Listener
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(state: ReleaseEpisodesControlState, place: EpisodeControlPlace) {
            full_button_continue.isEnabled = state.hasEpisodes
            full_button_web.isVisible = state.hasWeb
            full_button_continue.text = state.continueTitle

            if (state.hasViewed) {
                full_button_continue.setOnClickListener { itemListener.onClickContinue(place) }
            } else {
                full_button_continue.text = "Начать просмотр"
            }
            full_btn_episodes_menu.setOnClickListener { itemListener.onClickEpisodesMenu(place) }
            full_button_web.setOnClickListener { itemListener.onClickWatchWeb(place) }
        }
    }

    interface Listener {

        fun onClickWatchWeb(place: EpisodeControlPlace)

        fun onClickWatchAll(place: EpisodeControlPlace)

        fun onClickContinue(place: EpisodeControlPlace)

        fun onClickEpisodesMenu(place: EpisodeControlPlace)
    }
}