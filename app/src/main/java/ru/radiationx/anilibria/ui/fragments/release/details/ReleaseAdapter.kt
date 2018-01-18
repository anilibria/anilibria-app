package ru.radiationx.anilibria.ui.fragments.release.details

/* Created by radiationx on 18.11.17. */

import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import ru.radiationx.anilibria.entity.app.release.Comment
import ru.radiationx.anilibria.entity.app.release.ReleaseFull
import ru.radiationx.anilibria.ui.adapters.CommentListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseEpisodeListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseHeadListItem
import ru.radiationx.anilibria.ui.adapters.release.detail.CommentDelegate
import ru.radiationx.anilibria.ui.adapters.release.detail.ReleaseEpisodeDelegate
import ru.radiationx.anilibria.ui.adapters.release.detail.ReleaseHeadDelegate

class ReleaseAdapter(var itemListener: ItemListener) : ListDelegationAdapter<MutableList<ListItem>>() {

    init {
        items = mutableListOf()
        delegatesManager.run {
            addDelegate(ReleaseHeadDelegate(itemListener))
            addDelegate(ReleaseEpisodeDelegate(itemListener))
            addDelegate(CommentDelegate())
        }
    }

    fun setRelease(release: ReleaseFull) {
        items.clear()
        items.add(ReleaseHeadListItem(release))
        items.addAll(release.episodes.map { ReleaseEpisodeListItem(it) })
        notifyDataSetChanged()
    }

    fun setComments(comments: List<Comment>) {
        items.addAll(comments.map { CommentListItem(it) })
        notifyDataSetChanged()
    }

    interface ItemListener : ReleaseHeadDelegate.Listener, ReleaseEpisodeDelegate.Listener

}
