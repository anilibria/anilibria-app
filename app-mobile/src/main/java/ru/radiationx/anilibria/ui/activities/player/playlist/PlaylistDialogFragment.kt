package ru.radiationx.anilibria.ui.activities.player.playlist

import android.os.Bundle
import android.view.View
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.FragmentPlayerPlaylistBinding
import ru.radiationx.anilibria.ui.activities.player.di.SharedPlayerData
import ru.radiationx.anilibria.ui.adapters.FeedSectionListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.PlaylistEpisodeListItem
import ru.radiationx.anilibria.ui.adapters.feed.FeedSectionDelegate
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter
import ru.radiationx.anilibria.ui.fragments.AlertDialogFragment
import ru.radiationx.quill.inject
import ru.radiationx.shared.ktx.android.launchInResumed

class PlaylistDialogFragment : AlertDialogFragment(R.layout.fragment_player_playlist) {

    private val binding by viewBinding<FragmentPlayerPlaylistBinding>()

    private val playlistAdapter = ListItemAdapter().apply {
        addDelegate(PlaylistEpisodeDelegate {
            sharedPlayerData.onEpisodeSelected.set(it)
            dismissAllowingStateLoss()
        })
        addDelegate(FeedSectionDelegate {
            // do nothing
        })
    }

    private val sharedPlayerData by inject<SharedPlayerData>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = playlistAdapter
        }

        combine(
            sharedPlayerData.dataState.mapNotNull { it.data }.distinctUntilChanged(),
            sharedPlayerData.episodeId
        ) { data, episodeId ->
            val items = mutableListOf<ListItem>()
            val isSingle = data.releases.size <= 1
            data.releases.forEach { release ->
                if (!isSingle) {
                    val item = FeedSectionListItem(
                        tag = release.id.id.toString(),
                        title = release.name,
                    )
                    items.add(item)
                }

                release.episodes.forEach {
                    val item = PlaylistEpisodeListItem(
                        episode = it,
                        isPlaying = it.id == episodeId
                    )
                    items.add(item)
                }
            }
            val scrollPosition = items.indexOfFirst {
                it is PlaylistEpisodeListItem && it.episode.id == episodeId
            }
            playlistAdapter.setItems(items) {
                binding.root.scrollToPosition(scrollPosition)
            }
        }.launchInResumed(viewLifecycleOwner)
    }

    override fun onStart() {
        super.onStart()
        val window = requireDialog().window ?: return
        WindowCompat.getInsetsController(window, binding.root).apply {
            hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.root.adapter = null
    }
}