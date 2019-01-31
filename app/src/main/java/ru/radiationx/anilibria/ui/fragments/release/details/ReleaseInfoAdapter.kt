package ru.radiationx.anilibria.ui.fragments.release.details

/* Created by radiationx on 18.11.17. */

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.entity.app.release.ReleaseFull
import ru.radiationx.anilibria.entity.app.vital.VitalItem
import ru.radiationx.anilibria.ui.adapters.*
import ru.radiationx.anilibria.ui.adapters.global.CommentRouteDelegate
import ru.radiationx.anilibria.ui.adapters.other.DividerShadowItemDelegate
import ru.radiationx.anilibria.ui.adapters.release.detail.*
import java.util.*

class ReleaseInfoAdapter(private var itemListener: ItemListener) : ListDelegationAdapter<MutableList<ListItem>>() {

    companion object {
        val TYPE_HEAD = Pair(1, 1)
        val TYPE_EPISODE = Pair(2, 15)
        val TYPE_EPISODE_CONTROL = Pair(3, 1)
        val TYPE_EPISODES_HEAD = Pair(4, 1)
        val TYPE_DONATE = Pair(5, 1)
        val TYPE_REMIND = Pair(6, 1)
        val TYPE_BLOCKED = Pair(7, 1)
        val TYPE_ROUTE = Pair(8, 1)
        val TYPE_SHADOW = Pair(9, 5)
    }

    private val remindText = "Если серии всё ещё нет в плеере, воспользуйтесь торрентом или веб-плеером"
    private val vitalItems = mutableListOf<VitalItem>()

    private var currentRelease: ReleaseFull? = null
    private var currentTabTag = ReleaseEpisodesHeadDelegate.TAG_ONLINE
    private var reverseEpisodes = App.injections.appPreferences.getEpisodesIsReverse()
    private val remindCloseListener = object : ReleaseRemindDelegate.Listener {
        override fun onClickClose(position: Int) {
            items.removeAt(position)
            items.removeAt(position)
            notifyItemRangeRemoved(position, 2)
            App.injections.appPreferences.setReleaseRemind(false)
        }
    }

    private val episodeHeadListener = object : ReleaseEpisodesHeadDelegate.Listener {
        override fun onSelect(tabTag: String, position: Int) {
            currentTabTag = tabTag
            currentRelease?.let {
                val startPos = items.indexOfFirst { it is ReleaseEpisodeListItem }
                items.removeAll { it is ReleaseEpisodeListItem }
                items.addAll(startPos, prepareEpisodeItems(it))
                notifyItemRangeChanged(startPos, items.size)
                return@let
            }
        }
    }

    init {
        items = mutableListOf()
        delegatesManager.apply {
            addDelegate(TYPE_HEAD.first, ReleaseHeadDelegate(itemListener))
            addDelegate(TYPE_EPISODE.first, ReleaseEpisodeDelegate(itemListener))
            addDelegate(TYPE_EPISODE_CONTROL.first, ReleaseEpisodeControlDelegate(itemListener))
            addDelegate(TYPE_EPISODES_HEAD.first, ReleaseEpisodesHeadDelegate(episodeHeadListener))
            addDelegate(TYPE_DONATE.first, ReleaseDonateDelegate(itemListener))
            addDelegate(TYPE_REMIND.first, ReleaseRemindDelegate(remindCloseListener))
            addDelegate(TYPE_BLOCKED.first, ReleaseBlockedDelegate())
            addDelegate(TYPE_ROUTE.first, CommentRouteDelegate())
            addDelegate(TYPE_SHADOW.first, DividerShadowItemDelegate())
            addDelegate(VitalWebItemDelegate(true))
            addDelegate(VitalNativeItemDelegate(true))
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.recycledViewPool.apply {
            setMaxRecycledViews(TYPE_HEAD.first, TYPE_HEAD.second)
            setMaxRecycledViews(TYPE_EPISODE.first, TYPE_EPISODE.second)
            setMaxRecycledViews(TYPE_EPISODE_CONTROL.first, TYPE_EPISODE_CONTROL.second)
            setMaxRecycledViews(TYPE_EPISODES_HEAD.first, TYPE_EPISODES_HEAD.second)
            setMaxRecycledViews(TYPE_DONATE.first, TYPE_DONATE.second)
            setMaxRecycledViews(TYPE_REMIND.first, TYPE_REMIND.second)
            setMaxRecycledViews(TYPE_BLOCKED.first, TYPE_BLOCKED.second)
            setMaxRecycledViews(TYPE_ROUTE.first, TYPE_ROUTE.second)
            setMaxRecycledViews(TYPE_SHADOW.first, TYPE_SHADOW.second)
        }
    }

    private val random = Random()

    private fun rand(from: Int, to: Int): Int {
        return random.nextInt(to - from) + from
    }

    fun setVitals(vitals: List<VitalItem>) {
        vitalItems.clear()
        vitalItems.addAll(vitals)
    }

    private fun getVitalListItem(item: VitalItem) = when (item.contentType) {
        VitalItem.ContentType.WEB -> VitalWebListItem(item)
        else -> VitalNativeListItem(item)
    }

    fun setRelease(release: ReleaseFull) {
        items.clear()
        currentRelease = release
        items.add(ReleaseHeadListItem(release))
        items.add(DividerShadowListItem())

        if (release.blockedInfo.isBlocked) {
            items.add(ReleaseBlockedListItem(release))
            items.add(DividerShadowListItem())
        }

        if (!release.blockedInfo.isBlocked && release.episodes.isNotEmpty()) {
            items.add(ReleaseDonateListItem())
            items.add(DividerShadowListItem())
        }

        if (vitalItems.isNotEmpty()) {
            val randomVital = if (vitalItems.size > 1) rand(0, vitalItems.size) else 0
            val listItem = getVitalListItem(vitalItems[randomVital])
            this.items.add(listItem)
            items.add(DividerShadowListItem())
        }

        if (!release.blockedInfo.isBlocked && App.injections.appPreferences.getReleaseRemind()) {
            items.add(ReleaseRemindListItem(remindText))
            items.add(DividerShadowListItem())
        }

        if (release.episodes.isNotEmpty() || release.episodesSource.isNotEmpty()) {
            if (release.episodes.isNotEmpty()) {
                items.add(ReleaseEpisodeControlItem(release))
            }
            if (/*release.episodesSource.isNotEmpty() && */release.episodesSource.isNotEmpty()) {
                items.add(ReleaseEpisodesHeadListItem(currentTabTag))
            }
            items.addAll(prepareEpisodeItems(release))
            items.add(DividerShadowListItem())
        }

        items.add(CommentRouteListItem())
        items.add(DividerShadowListItem())

        notifyDataSetChanged()
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

    interface ItemListener :
            ReleaseHeadDelegate.Listener,
            ReleaseEpisodeDelegate.Listener,
            ReleaseDonateDelegate.Listener,
            ReleaseEpisodeControlDelegate.Listener

}
