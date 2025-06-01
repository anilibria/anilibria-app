package ru.radiationx.anilibria.ui.activities.player.playlist

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import dev.androidbroadcast.vbpd.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemPlaylistEpisodeBinding
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.PlaylistEpisodeListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.data.common.EpisodeId

class PlaylistEpisodeDelegate(
    private val clickListener: (EpisodeId) -> Unit,
) : AppAdapterDelegate<PlaylistEpisodeListItem, ListItem, PlaylistEpisodeDelegate.ViewHolder>(
    R.layout.item_playlist_episode,
    { it is PlaylistEpisodeListItem },
    { ViewHolder(it, clickListener) }
) {

    override fun bindData(item: PlaylistEpisodeListItem, holder: ViewHolder) =
        holder.bind(item)

    class ViewHolder(
        itemView: View,
        private val clickListener: (EpisodeId) -> Unit,
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemPlaylistEpisodeBinding>()

        fun bind(item: PlaylistEpisodeListItem) {
            binding.episodeDot.isVisible = item.isPlaying
            binding.episodeTitle.text = item.episode.title
            binding.root.setOnClickListener { clickListener.invoke(item.episode.id) }
        }
    }
}
