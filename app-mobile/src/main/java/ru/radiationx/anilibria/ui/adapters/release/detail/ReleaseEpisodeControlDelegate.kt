package ru.radiationx.anilibria.ui.adapters.release.detail

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemReleaseEpisodeControlBinding
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseEpisodeControlItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.fragments.release.details.ReleaseEpisodesControlState

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
        itemView: View,
        private val itemListener: Listener
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemReleaseEpisodeControlBinding>()

        fun bind(state: ReleaseEpisodesControlState, place: EpisodeControlPlace) {
            binding.fullBtnEpisodesMenu.isVisible = state.hasEpisodes
            binding.fullButtonContinue.isVisible = state.hasEpisodes
            binding.fullButtonWeb.isVisible = state.hasWeb
            binding.fullButtonContinue.text = state.continueTitle

            if (state.hasViewed) {
                binding.fullButtonContinue.setOnClickListener { itemListener.onClickContinue(place) }
            } else {
                binding.fullButtonContinue.setOnClickListener { itemListener.onClickWatchAll(place) }
            }
            binding.fullBtnEpisodesMenu.setOnClickListener { itemListener.onClickEpisodesMenu(place) }
            binding.fullButtonWeb.setOnClickListener { itemListener.onClickWatchWeb(place) }
        }
    }

    interface Listener {

        fun onClickWatchWeb(place: EpisodeControlPlace)

        fun onClickWatchAll(place: EpisodeControlPlace)

        fun onClickContinue(place: EpisodeControlPlace)

        fun onClickEpisodesMenu(place: EpisodeControlPlace)
    }
}