package ru.radiationx.anilibria.ui.fragments.release.details

/* Created by radiationx on 18.11.17. */

import android.util.Log
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.entity.app.release.ReleaseFull
import ru.radiationx.anilibria.entity.app.vital.VitalItem
import ru.radiationx.anilibria.ui.adapters.*
import ru.radiationx.anilibria.ui.adapters.global.CommentRouteDelegate
import ru.radiationx.anilibria.ui.adapters.other.DividerShadowItemDelegate
import ru.radiationx.anilibria.ui.adapters.release.detail.*
import java.util.*
import kotlin.math.min

class ReleaseAdapter(private var itemListener: ItemListener) : ListDelegationAdapter<MutableList<ListItem>>() {

    private val vitalItems = mutableListOf<VitalItem>()

    private var currentRelease: ReleaseFull? = null
    private var currentTabTag = ReleaseEpisodesHeadDelegate.TAG_ONLINE
    private var reverseEpisodes = App.injections.appPreferences.getEpisodesIsReverse()
    private val remindCloseListener = object : ReleaseRemindDelegate.Listener {
        override fun onClickClose(position: Int) {
            Log.e("SUKA", "onClickClose: $position")
            items.removeAt(position)
            items.removeAt(position)
            notifyItemRangeRemoved(position, position + 1)
            App.injections.appPreferences.setReleaseRemind(false)
        }
    }

    private val episodeHeadListener = object : ReleaseEpisodesHeadDelegate.Listener {
        override fun onSelect(tabTag: String, position: Int) {
            currentTabTag = tabTag
            currentRelease?.let {
                val startPos = items.indexOfFirst { it is ReleaseEpisodeListItem }
                items.removeAll { it is ReleaseEpisodeListItem }
                val newItems = when (tabTag) {
                    ReleaseEpisodesHeadDelegate.TAG_ONLINE -> it.episodes.map { ReleaseEpisodeListItem(it) }
                    ReleaseEpisodesHeadDelegate.TAG_DOWNLOAD -> it.episodesSource.map { ReleaseEpisodeListItem(it) }
                    else -> emptyList()
                }.toMutableList()

                if (reverseEpisodes) {
                    newItems.reverse()
                }
                items.addAll(startPos, newItems)

                notifyItemRangeChanged(startPos, items.size)
                return@let
            }
        }
    }

    init {
        items = mutableListOf()
        delegatesManager.run {
            addDelegate(ReleaseHeadDelegate(itemListener))
            addDelegate(ReleaseEpisodeDelegate(itemListener))
            addDelegate(ReleaseEpisodesHeadDelegate(episodeHeadListener))
            addDelegate(ReleaseDonateDelegate(itemListener))
            addDelegate(ReleaseRemindDelegate(remindCloseListener))
            addDelegate(ReleaseBlockedDelegate())
            addDelegate(CommentRouteDelegate())
            addDelegate(DividerShadowItemDelegate())
            addDelegate(VitalWebItemDelegate(true))
            addDelegate(VitalNativeItemDelegate(true))
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

        if (release.isBlocked) {
            items.add(ReleaseBlockedListItem(release))
            items.add(DividerShadowListItem())
        }

        if (!release.isBlocked && release.episodes.isNotEmpty()) {
            items.add(ReleaseDonateListItem())
            items.add(DividerShadowListItem())
        }

        if (vitalItems.isNotEmpty()) {
            val randomVital = if (vitalItems.size > 1) rand(0, vitalItems.size) else 0
            val listItem = getVitalListItem(vitalItems[randomVital])
            this.items.add(listItem)
            items.add(DividerShadowListItem())
        }

        if (!release.isBlocked && App.injections.appPreferences.getReleaseRemind()) {
            items.add(ReleaseRemindListItem())
            items.add(DividerShadowListItem())
        }

        if (release.episodes.isNotEmpty() || release.episodesSource.isNotEmpty()) {
            if (release.episodesSource.isNotEmpty() && release.episodesSource.isNotEmpty()) {
                items.add(ReleaseEpisodesHeadListItem(currentTabTag))
            }
            val newItems = when (currentTabTag) {
                ReleaseEpisodesHeadDelegate.TAG_ONLINE -> release.episodes.map { ReleaseEpisodeListItem(it) }
                ReleaseEpisodesHeadDelegate.TAG_DOWNLOAD -> release.episodesSource.map { ReleaseEpisodeListItem(it) }
                else -> emptyList()
            }.toMutableList()
            if (reverseEpisodes) {
                newItems.reverse()
            }
            items.addAll(newItems)
            items.add(DividerShadowListItem())
        }

        items.add(CommentRouteListItem())
        items.add(DividerShadowListItem())

        notifyDataSetChanged()
    }


    interface ItemListener : ReleaseHeadDelegate.Listener, ReleaseEpisodeDelegate.Listener, ReleaseDonateDelegate.Listener

}
