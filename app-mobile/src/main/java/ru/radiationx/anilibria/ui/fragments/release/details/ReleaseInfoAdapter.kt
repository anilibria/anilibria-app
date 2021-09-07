package ru.radiationx.anilibria.ui.fragments.release.details

/* Created by radiationx on 18.11.17. */

import ru.radiationx.anilibria.ui.adapters.*
import ru.radiationx.anilibria.ui.adapters.feed.FeedSectionDelegate
import ru.radiationx.anilibria.ui.adapters.global.CommentRouteDelegate
import ru.radiationx.anilibria.ui.adapters.other.DividerShadowItemDelegate
import ru.radiationx.anilibria.ui.adapters.release.detail.*
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.entity.app.release.ReleaseFull
import ru.radiationx.data.entity.app.release.TorrentItem
import ru.radiationx.shared_app.di.DI
import java.util.*

class ReleaseInfoAdapter(
    private val headListener: ReleaseHeadDelegate.Listener,
    private val episodeListener: ReleaseEpisodeDelegate.Listener,
    private val episodeControlListener: ReleaseEpisodeControlDelegate.Listener,
    private val donateListener: ReleaseDonateDelegate.Listener,
    private val torrentClickListener: (TorrentItem) -> Unit,
    private val commentsClickListener: () -> Unit
) : ListItemAdapter() {

    private val localItems = mutableListOf<ListItem>()

    private val appPreferences: PreferencesHolder = DI.get(PreferencesHolder::class.java)

    private val remindText =
        "Если серии всё ещё нет в плеере, воспользуйтесь торрентом или веб-плеером"

    private var currentRelease: ReleaseFull? = null
    private var currentTabTag = ReleaseEpisodesHeadDelegate.TAG_ONLINE

    private var currentTorrentsExpand = false

    private val torrentsListItem = ReleaseExpandListItem("Показать все")

    private var reverseEpisodes = appPreferences.getEpisodesIsReverse()
    private val remindCloseListener = object : ReleaseRemindDelegate.Listener {
        override fun onClickClose(position: Int) {
            localItems.removeAt(position)
            localItems.removeAt(position)
            notifyDiffItems()
            appPreferences.setReleaseRemind(false)
        }
    }

    private val episodeHeadListener = object : ReleaseEpisodesHeadDelegate.Listener {
        override fun onSelect(tabTag: String, position: Int) {
            currentTabTag = tabTag
            currentRelease?.let {
                val startPos = localItems.indexOfFirst { it is ReleaseEpisodeListItem }
                localItems.removeAll { it is ReleaseEpisodeListItem }
                localItems.addAll(startPos, prepareEpisodeItems(it))
                notifyDiffItems()
                return@let
            }
        }
    }

    init {
        items = mutableListOf()
        addDelegate(ReleaseHeadDelegate(headListener))
        addDelegate(FeedSectionDelegate {})
        addDelegate(ReleaseExpandDelegate {
            when (it) {
                torrentsListItem -> {
                    currentTorrentsExpand = true
                    currentRelease?.also { it1 -> setRelease(it1) }
                }
            }
        })
        addDelegate(ReleaseEpisodeDelegate(episodeListener))
        addDelegate(ReleaseTorrentDelegate(torrentClickListener))
        addDelegate(ReleaseEpisodeControlDelegate(episodeControlListener))
        addDelegate(ReleaseEpisodesHeadDelegate(episodeHeadListener))
        addDelegate(ReleaseDonateDelegate(donateListener))
        addDelegate(ReleaseRemindDelegate(remindCloseListener))
        addDelegate(ReleaseBlockedDelegate())
        addDelegate(CommentRouteDelegate(commentsClickListener))
        addDelegate(DividerShadowItemDelegate())
    }

    fun setRelease(release: ReleaseFull) {
        localItems.clear()
        currentRelease = release
        localItems.add(ReleaseEpisodeControlItem(release, false, EpisodeControlPlace.TOP))
        localItems.add(ReleaseHeadListItem(release))
        localItems.add(DividerShadowListItem("head"))

        if (release.blockedInfo.isBlocked) {
            localItems.add(ReleaseBlockedListItem(release))
            localItems.add(DividerShadowListItem("blocked"))
        }

        if (!release.blockedInfo.isBlocked && release.episodes.isNotEmpty()) {
            localItems.add(ReleaseDonateListItem("donate"))
            localItems.add(DividerShadowListItem("donate"))
        }

        val torrents = release.torrents.asReversed()
        if (torrents.isNotEmpty()) {
            localItems.add(FeedSectionListItem("Раздачи", hasBg = true))
            if (!currentTorrentsExpand && release.torrents.size > 3) {
                localItems.addAll(torrents.take(3).map { ReleaseTorrentListItem(it) })
                localItems.add(torrentsListItem)
            } else {
                localItems.addAll(torrents.map { ReleaseTorrentListItem(it) })
            }
            localItems.add(DividerShadowListItem("torrents"))
        }

        if (!release.blockedInfo.isBlocked && appPreferences.getReleaseRemind()) {
            localItems.add(ReleaseRemindListItem(remindText))
            localItems.add(DividerShadowListItem("remind"))
        }

        if (release.episodes.isNotEmpty() || release.episodesSource.isNotEmpty()) {
            if (release.episodes.isNotEmpty()) {
                localItems.add(
                    ReleaseEpisodeControlItem(
                        release,
                        release.moonwalkLink != null,
                        EpisodeControlPlace.BOTTOM
                    )
                )
            }
            if (/*release.episodesSource.isNotEmpty() && */release.episodesSource.isNotEmpty()) {
                localItems.add(ReleaseEpisodesHeadListItem(currentTabTag))
            }
            localItems.addAll(prepareEpisodeItems(release))
            localItems.add(DividerShadowListItem("episodes"))
        }

        localItems.add(CommentRouteListItem("comments"))
        localItems.add(DividerShadowListItem("comments"))

        notifyDiffItems()
    }

    private fun notifyDiffItems() {
        items = localItems.toList()
    }

    private fun prepareEpisodeItems(release: ReleaseFull): List<ReleaseEpisodeListItem> {
        val newItems = when (currentTabTag) {
            ReleaseEpisodesHeadDelegate.TAG_ONLINE -> release.episodes.mapIndexed { index, episode ->
                ReleaseEpisodeListItem(episode, index % 2 == 0)
            }
            ReleaseEpisodesHeadDelegate.TAG_DOWNLOAD -> release.episodesSource.mapIndexed { index, episode ->
                ReleaseEpisodeListItem(episode, index % 2 == 0)
            }
            else -> emptyList()
        }.toMutableList()
        if (reverseEpisodes) {
            newItems.reverse()
        }
        return newItems
    }

    interface ItemListener

}
