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

class ReleaseAdapter(private var itemListener: ItemListener) : ListDelegationAdapter<MutableList<ListItem>>() {

    private val vitalItems = mutableListOf<VitalItem>()

    private val remindCloseListener = object : ReleaseRemindDelegate.Listener {
        override fun onClickClose(position: Int) {
            Log.e("SUKA", "onClickClose: $position")
            items.removeAt(position)
            items.removeAt(position)
            notifyItemRangeRemoved(position, position + 1)
            App.injections.appPreferences.setReleaseRemind(false)
        }
    }

    init {
        items = mutableListOf()
        delegatesManager.run {
            addDelegate(ReleaseHeadDelegate(itemListener))
            addDelegate(ReleaseEpisodeDelegate(itemListener))
            addDelegate(ReleaseEpisodesHeadDelegate())
            addDelegate(ReleaseDonateDelegate(itemListener))
            addDelegate(ReleaseRemindDelegate(remindCloseListener))
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
        items.add(ReleaseHeadListItem(release))
        if (release.episodes.isNotEmpty()) {
            items.add(DividerShadowListItem())
            items.add(ReleaseDonateListItem())
            items.add(DividerShadowListItem())
            //items.add(ReleaseEpisodesHeadListItem())
        }
        if (vitalItems.isNotEmpty()) {
            val randomVital = if (vitalItems.size > 1) rand(0, vitalItems.size) else 0
            val listItem = getVitalListItem(vitalItems[randomVital])
            this.items.add(listItem)
            items.add(DividerShadowListItem())
        }

        if (App.injections.appPreferences.getReleaseRemind()) {
            items.add(ReleaseRemindListItem())
            items.add(DividerShadowListItem())
        }
        items.addAll(release.episodes.map { ReleaseEpisodeListItem(it) })
        items.add(DividerShadowListItem())
        items.add(CommentRouteListItem())
        items.add(DividerShadowListItem())
        notifyDataSetChanged()
    }


    interface ItemListener : ReleaseHeadDelegate.Listener, ReleaseEpisodeDelegate.Listener, ReleaseDonateDelegate.Listener

}
