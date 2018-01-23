package ru.radiationx.anilibria.ui.fragments.release.details

/* Created by radiationx on 18.11.17. */

import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import ru.radiationx.anilibria.entity.app.release.ReleaseFull
import ru.radiationx.anilibria.ui.adapters.*
import ru.radiationx.anilibria.ui.adapters.global.CommentRouteDelegate
import ru.radiationx.anilibria.ui.adapters.other.DividerShadowItemDelegate
import ru.radiationx.anilibria.ui.adapters.release.detail.ReleaseDonateDelegate
import ru.radiationx.anilibria.ui.adapters.release.detail.ReleaseEpisodeDelegate
import ru.radiationx.anilibria.ui.adapters.release.detail.ReleaseEpisodesHeadDelegate
import ru.radiationx.anilibria.ui.adapters.release.detail.ReleaseHeadDelegate

class ReleaseAdapter(var itemListener: ItemListener) : ListDelegationAdapter<MutableList<ListItem>>() {

    init {
        items = mutableListOf()
        delegatesManager.run {
            addDelegate(ReleaseHeadDelegate(itemListener))
            addDelegate(ReleaseEpisodeDelegate(itemListener))
            addDelegate(ReleaseEpisodesHeadDelegate())
            addDelegate(ReleaseDonateDelegate(itemListener))
            addDelegate(CommentRouteDelegate())
            addDelegate(DividerShadowItemDelegate())
        }
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
        items.addAll(release.episodes.map { ReleaseEpisodeListItem(it) })
        items.add(DividerShadowListItem())
        items.add(CommentRouteListItem())
        items.add(DividerShadowListItem())
        notifyDataSetChanged()
    }


    interface ItemListener : ReleaseHeadDelegate.Listener, ReleaseEpisodeDelegate.Listener, ReleaseDonateDelegate.Listener

}
