package ru.radiationx.anilibria.ui.fragments.release.details

/* Created by radiationx on 18.11.17. */

import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import ru.radiationx.anilibria.entity.app.release.ReleaseFull
import ru.radiationx.anilibria.ui.adapters.*
import ru.radiationx.anilibria.ui.adapters.global.CommentRouteDelegate
import ru.radiationx.anilibria.ui.adapters.other.DividerShadowItemDelegate
import ru.radiationx.anilibria.ui.adapters.release.detail.ReleaseEpisodeDelegate
import ru.radiationx.anilibria.ui.adapters.release.detail.ReleaseHeadDelegate

class ReleaseAdapter(var itemListener: ItemListener) : ListDelegationAdapter<MutableList<ListItem>>() {

    init {
        items = mutableListOf()
        delegatesManager.run {
            addDelegate(ReleaseHeadDelegate(itemListener))
            addDelegate(ReleaseEpisodeDelegate(itemListener))
            addDelegate(CommentRouteDelegate())
            addDelegate(DividerShadowItemDelegate())
        }
    }

    fun setRelease(release: ReleaseFull) {
        items.clear()
        items.add(ReleaseHeadListItem(release))
        items.addAll(release.episodes.map { ReleaseEpisodeListItem(it) })
        items.add(DividerShadowListItem())
        items.add(CommentRouteListItem())
        items.add(DividerShadowListItem())
        notifyDataSetChanged()
    }


    interface ItemListener : ReleaseHeadDelegate.Listener, ReleaseEpisodeDelegate.Listener

}
