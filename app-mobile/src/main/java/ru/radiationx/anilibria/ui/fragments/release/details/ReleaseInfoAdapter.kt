package ru.radiationx.anilibria.ui.fragments.release.details

/* Created by radiationx on 18.11.17. */

import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.extension.setAndAwaitItems
import ru.radiationx.anilibria.model.DonationCardItemState
import ru.radiationx.anilibria.ui.adapters.CommentRouteListItem
import ru.radiationx.anilibria.ui.adapters.DividerShadowListItem
import ru.radiationx.anilibria.ui.adapters.FeedSectionListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.NativeAdListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseBlockedListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseDonateListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseEpisodeControlItem
import ru.radiationx.anilibria.ui.adapters.ReleaseEpisodeListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseEpisodesHeadListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseFranchiseHeaderListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseFranchiseListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseHeadListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseRemindListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseSponsorListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseTorrentListItem
import ru.radiationx.anilibria.ui.adapters.ShadowDirection
import ru.radiationx.anilibria.ui.adapters.ads.NativeAdDelegate
import ru.radiationx.anilibria.ui.adapters.feed.FeedSectionDelegate
import ru.radiationx.anilibria.ui.adapters.global.CommentRouteDelegate
import ru.radiationx.anilibria.ui.adapters.other.DividerShadowItemDelegate
import ru.radiationx.anilibria.ui.adapters.release.detail.EpisodeControlPlace
import ru.radiationx.anilibria.ui.adapters.release.detail.ReleaseBlockedDelegate
import ru.radiationx.anilibria.ui.adapters.release.detail.ReleaseDonateDelegate
import ru.radiationx.anilibria.ui.adapters.release.detail.ReleaseEpisodeControlDelegate
import ru.radiationx.anilibria.ui.adapters.release.detail.ReleaseEpisodeDelegate
import ru.radiationx.anilibria.ui.adapters.release.detail.ReleaseEpisodesHeadDelegate
import ru.radiationx.anilibria.ui.adapters.release.detail.ReleaseExpandDelegate
import ru.radiationx.anilibria.ui.adapters.release.detail.ReleaseFranchiseDelegate
import ru.radiationx.anilibria.ui.adapters.release.detail.ReleaseFranchiseHeaderDelegate
import ru.radiationx.anilibria.ui.adapters.release.detail.ReleaseHeadDelegate
import ru.radiationx.anilibria.ui.adapters.release.detail.ReleaseRemindDelegate
import ru.radiationx.anilibria.ui.adapters.release.detail.ReleaseSponsorDelegate
import ru.radiationx.anilibria.ui.adapters.release.detail.ReleaseTorrentDelegate
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter
import ru.radiationx.data.api.releases.models.ReleaseSponsor
import ru.radiationx.data.common.ReleaseId
import ru.radiationx.data.common.TorrentId

class ReleaseInfoAdapter(
    headListener: ReleaseHeadDelegate.Listener,
    episodeListener: ReleaseEpisodeDelegate.Listener,
    episodeControlListener: ReleaseEpisodeControlDelegate.Listener,
    donationListener: (DonationCardItemState) -> Unit,
    sponsorListener: (ReleaseSponsor) -> Unit,
    donationCloseListener: (DonationCardItemState) -> Unit,
    torrentClickListener: (TorrentId) -> Unit,
    torrentCancelClickListener: (TorrentId) -> Unit,
    commentsClickListener: () -> Unit,
    episodesTabListener: (String) -> Unit,
    remindCloseListener: () -> Unit,
    franchiseClickListener: (ReleaseId) -> Unit,
    private val torrentInfoListener: () -> Unit,
) : ListItemAdapter() {

    companion object {
        private const val TORRENT_TAG = "torrents"
        private const val FRANCHISE_TAG = "franchise"
    }

    init {
        addDelegate(ReleaseHeadDelegate(headListener))
        addDelegate(FeedSectionDelegate {
            if (it.tag == TORRENT_TAG) {
                torrentInfoListener.invoke()
            }
        })
        addDelegate(ReleaseExpandDelegate {})
        addDelegate(ReleaseEpisodeDelegate(episodeListener))
        addDelegate(ReleaseTorrentDelegate(torrentClickListener, torrentCancelClickListener))
        addDelegate(ReleaseEpisodeControlDelegate(episodeControlListener))
        addDelegate(ReleaseEpisodesHeadDelegate(episodesTabListener))
        addDelegate(ReleaseDonateDelegate(donationListener, donationCloseListener))
        addDelegate(ReleaseSponsorDelegate(sponsorListener))
        addDelegate(ReleaseRemindDelegate(remindCloseListener))
        addDelegate(ReleaseBlockedDelegate())
        addDelegate(CommentRouteDelegate(commentsClickListener))
        addDelegate(DividerShadowItemDelegate())
        addDelegate(NativeAdDelegate())
        addDelegate(ReleaseFranchiseHeaderDelegate())
        addDelegate(ReleaseFranchiseDelegate(franchiseClickListener))
    }

    suspend fun bindState(releaseState: ReleaseDetailState, screenState: ReleaseDetailScreenState) {
        val modifications = screenState.modifiers
        val newItems = mutableListOf<ListItem>()

        if (releaseState.episodesControl != null && releaseState.episodesControl.hasEpisodes) {
            newItems.add(
                ReleaseEpisodeControlItem(
                    releaseState.episodesControl.copy(hasWeb = false),
                    EpisodeControlPlace.TOP
                )
            )
        } else if (modifications.detailLoading) {
            newItems.add(
                ReleaseEpisodeControlItem(
                    ReleaseEpisodesControlState(
                        hasWeb = false,
                        hasEpisodes = true,
                        hasViewed = false,
                        continueTitle = "Загрузка..."
                    ),
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
        newItems.add(DividerShadowListItem(ShadowDirection.Double, "head"))

        if (releaseState.sponsor != null) {
            newItems.add(ReleaseSponsorListItem(releaseState.sponsor))
            newItems.add(DividerShadowListItem(ShadowDirection.Double, "sponsor"))
        }

        if (releaseState.blockedInfo != null) {
            newItems.add(ReleaseBlockedListItem(releaseState.blockedInfo))
            newItems.add(DividerShadowListItem(ShadowDirection.Double, "blocked"))
        }

        if (releaseState.blockedInfo == null && screenState.donationCardState != null) {
            newItems.add(ReleaseDonateListItem(screenState.donationCardState))
            newItems.add(DividerShadowListItem(ShadowDirection.Double, "donate"))
        }

        if (releaseState.franchises.isNotEmpty()) {
            newItems.add(
                FeedSectionListItem(
                    tag = FRANCHISE_TAG,
                    title = "Связанное",
                    hasBg = true
                )
            )
            releaseState.franchises.forEach { franchise ->
                newItems.add(ReleaseFranchiseHeaderListItem(franchise.header))
                newItems.addAll(franchise.releases.map { ReleaseFranchiseListItem(it) })
                newItems.add(DividerShadowListItem(ShadowDirection.Double, franchise.header.id))
            }
        }

        if (releaseState.torrents.isNotEmpty()) {
            newItems.add(
                FeedSectionListItem(
                    TORRENT_TAG,
                    "Torrent раздачи",
                    null,
                    R.drawable.ic_help_circle_outline,
                    hasBg = true
                )
            )
            newItems.addAll(releaseState.torrents.map { ReleaseTorrentListItem(it) })
            newItems.add(DividerShadowListItem(ShadowDirection.Double, TORRENT_TAG))
        }

        if (releaseState.blockedInfo == null && screenState.remindText != null) {
            newItems.add(ReleaseRemindListItem(screenState.remindText))
            newItems.add(DividerShadowListItem(ShadowDirection.Double, "remind"))
        }

        if (releaseState.episodesControl != null) {
            newItems.add(
                ReleaseEpisodeControlItem(
                    releaseState.episodesControl,
                    EpisodeControlPlace.BOTTOM
                )
            )
        }
        if (releaseState.episodesTabs.isNotEmpty()) {
            val selectedEpisodesTabTag =
                modifications.selectedEpisodesTabTag ?: releaseState.episodesTabs.firstOrNull()?.tag

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
            // yes, this ok
            if (modifications.episodesReversed) {
                newItems.addAll(episodeListItems)
            } else {
                newItems.addAll(episodeListItems.asReversed())
            }
            newItems.add(DividerShadowListItem(ShadowDirection.Double, "episodes"))
        }

        val commentsShadow = if (screenState.nativeAd != null) {
            ShadowDirection.Double
        } else {
            ShadowDirection.Bottom
        }
        newItems.add(CommentRouteListItem("comments"))
        newItems.add(DividerShadowListItem(commentsShadow, "comments"))

        if (screenState.nativeAd != null) {
            newItems.add(NativeAdListItem(screenState.nativeAd))
            newItems.add(DividerShadowListItem(ShadowDirection.Bottom, "nativeAd"))
        }

        setAndAwaitItems(newItems)
    }
}
