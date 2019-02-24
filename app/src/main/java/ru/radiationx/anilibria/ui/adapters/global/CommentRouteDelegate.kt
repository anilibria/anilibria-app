package ru.radiationx.anilibria.ui.adapters.global

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.adapters.CommentRouteListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeDelegate

/**
 * Created by radiationx on 21.01.18.
 */
class CommentRouteDelegate : AppAdapterDelegate<CommentRouteListItem, ListItem, CommentRouteDelegate.ViewHolder>(
        R.layout.item_comment_route,
        { it is CommentRouteListItem },
        { ViewHolder(it) }
) {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}