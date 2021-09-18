package ru.radiationx.anilibria.ui.fragments.release.details

/* Created by radiationx on 18.11.17. */

import ru.radiationx.anilibria.presentation.release.details.ReleaseDetailScreenState
import ru.radiationx.anilibria.presentation.release.details.ReleaseDetailState
import ru.radiationx.anilibria.presentation.release.details.ReleaseTorrentItemState
import ru.radiationx.anilibria.ui.adapters.*
import ru.radiationx.anilibria.ui.adapters.feed.FeedSectionDelegate
import ru.radiationx.anilibria.ui.adapters.global.CommentRouteDelegate
import ru.radiationx.anilibria.ui.adapters.other.DividerShadowItemDelegate
import ru.radiationx.anilibria.ui.adapters.release.detail.*
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter

class ReleaseInfoAdapter(
    private val headListener: ReleaseHeadDelegate.Listener,
    private val episodeListener: ReleaseEpisodeDelegate.Listener,
    private val episodeControlListener: ReleaseEpisodeControlDelegate.Listener,
    private val donateListener: ReleaseDonateDelegate.Listener,
    private val torrentClickListener: (ReleaseTorrentItemState) -> Unit,
    private val commentsClickListener: () -> Unit,
    private val episodesTabListener: (String) -> Unit,
    private val remindCloseListener: () -> Unit
) : ListItemAdapter() {

    init {
        addDelegate(ReleaseHeadDelegate(headListener))
        addDelegate(FeedSectionDelegate {})
        addDelegate(ReleaseExpandDelegate {})
        addDelegate(ReleaseEpisodeDelegate(episodeListener))
        addDelegate(ReleaseTorrentDelegate(torrentClickListener))
        addDelegate(ReleaseEpisodeControlDelegate(episodeControlListener))
        addDelegate(ReleaseEpisodesHeadDelegate(episodesTabListener))
        addDelegate(ReleaseDonateDelegate(donateListener))
        addDelegate(ReleaseRemindDelegate(remindCloseListener))
        addDelegate(ReleaseBlockedDelegate())
        addDelegate(CommentRouteDelegate(commentsClickListener))
        addDelegate(DividerShadowItemDelegate())
    }

    fun bindState(releaseState: ReleaseDetailState, screenState: ReleaseDetailScreenState) {
        val modifications = screenState.modifiers
        val newItems = mutableListOf<ListItem>()

        if (releaseState.episodesControl != null) {
            newItems.add(
                ReleaseEpisodeControlItem(
                    releaseState.episodesControl.copy(hasWeb = false),
                    EpisodeControlPlace.TOP
                )
            )
        }

        newItems.add(
            ReleaseHeadListItem(
                "head",
                releaseState.info,
                modifications
            )
        )
        newItems.add(DividerShadowListItem("head"))

        if (releaseState.blockedInfo != null) {
            newItems.add(ReleaseBlockedListItem(releaseState.blockedInfo))
            newItems.add(DividerShadowListItem("blocked"))
        }

        if (releaseState.blockedInfo == null) {
            newItems.add(ReleaseDonateListItem("donate"))
            newItems.add(DividerShadowListItem("donate"))
        }

        if (releaseState.torrents.isNotEmpty()) {
            newItems.add(FeedSectionListItem("torrents", "Раздачи", null, hasBg = true))
            newItems.addAll(releaseState.torrents.map { ReleaseTorrentListItem(it) })
            newItems.add(DividerShadowListItem("torrents"))
        }

        if (releaseState.blockedInfo == null && screenState.remindText != null) {
            newItems.add(ReleaseRemindListItem(screenState.remindText))
            newItems.add(DividerShadowListItem("remind"))
        }

        if (releaseState.episodesTabs.isNotEmpty()) {
            val selectedEpisodesTabTag =
                modifications.selectedEpisodesTabTag ?: releaseState.episodesTabs.firstOrNull()?.tag
            if (releaseState.episodesControl != null) {
                newItems.add(
                    ReleaseEpisodeControlItem(
                        releaseState.episodesControl,
                        EpisodeControlPlace.BOTTOM
                    )
                )
            }

            if (releaseState.episodesTabs.size > 1) {
                newItems.add(
                    ReleaseEpisodesHeadListItem(
                        "tabs",
                        releaseState.episodesTabs,
                        selectedEpisodesTabTag
                    )
                )
            }

            val episodes = releaseState.episodesTabs
                .firstOrNull { it.tag == selectedEpisodesTabTag }
                ?.episodes.orEmpty()

            val episodeListItems = episodes.mapIndexed { index, episode ->
                ReleaseEpisodeListItem(episode, index % 2 == 0)
            }
            if (modifications.episodesReversed) {
                newItems.addAll(episodeListItems.asReversed())
            } else {
                newItems.addAll(episodeListItems)
            }

            newItems.add(DividerShadowListItem("episodes"))
        }

        newItems.add(CommentRouteListItem("comments"))
        newItems.add(DividerShadowListItem("comments"))

        items = newItems
    }
}
