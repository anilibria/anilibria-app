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
            currentRelease?.let {
                val lastCount = items.count { it is ReleaseEpisodeListItem }
                items.removeAll { it is ReleaseEpisodeListItem }
                val newItems = when (tabTag) {
                    "online" -> it.episodes.map { ReleaseEpisodeListItem(it) }
                    "download" -> it.episodesSource.map { ReleaseEpisodeListItem(it) }
                    else -> emptyList()
                }
                val startPos = position + 1
                items.addAll(startPos, newItems)

                notifyItemRangeChanged(startPos, items.size)
                /*val newCount = items.count { it is ReleaseEpisodeListItem }
                val changed = min(lastCount, newCount)
                val removed = lastCount - newCount
                val added = newCount - lastCount

                Log.e("SUKA", "CHECK COUNTS: l=$lastCount, n=$newCount, ch=$changed, rem=$removed, add=$added")

                val startChanged = startPos + changed

                Log.e("SUKA", "range ch: $startPos - $startChanged")
                notifyItemRangeChanged(startPos, startChanged)
                if (removed > 0) {
                    Log.e("SUKA", "range rem: $startChanged - ${startChanged + removed}")
                    notifyItemRangeRemoved(startChanged, startChanged + removed)
                }
                if (added > 0) {
                    Log.e("SUKA", "range add: $startChanged - ${startChanged + added}")
                    notifyItemRangeInserted(startChanged, startChanged + added)
                }*/
                //Log.e("SUKA", "changed: ${position + 1}, ${min(position + items.count { it is ReleaseEpisodeListItem }, items.size)}, max: ${items.size}")
                //notifyItemRangeChanged(position + 1, min(position + items.count { it is ReleaseEpisodeListItem }, items.size))
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

        if (release.episodes.isNotEmpty()) {
            if (release.episodesSource.isNotEmpty()) {
                items.add(ReleaseEpisodesHeadListItem())
            }
            items.addAll(release.episodes.map { ReleaseEpisodeListItem(it) })
            items.add(DividerShadowListItem())
        }

        items.add(CommentRouteListItem())
        items.add(DividerShadowListItem())

        notifyDataSetChanged()
    }


    interface ItemListener : ReleaseHeadDelegate.Listener, ReleaseEpisodeDelegate.Listener, ReleaseDonateDelegate.Listener

}
