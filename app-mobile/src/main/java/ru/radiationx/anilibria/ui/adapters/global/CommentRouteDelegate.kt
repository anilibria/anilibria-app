package ru.radiationx.anilibria.ui.adapters.global

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.adapters.CommentRouteListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate

/**
 * Created by radiationx on 21.01.18.
 */
class CommentRouteDelegate(
    private val clickListener: () -> Unit
) : AppAdapterDelegate<CommentRouteListItem, ListItem, CommentRouteDelegate.ViewHolder>(
    R.layout.item_comment_route,
    { it is CommentRouteListItem },
    { ViewHolder(it, clickListener) }
) {
    class ViewHolder(view: View, clickListener: () -> Unit) : RecyclerView.ViewHolder(view) {
        init {
            view.setOnClickListener { clickListener.invoke() }
        }
    }
}