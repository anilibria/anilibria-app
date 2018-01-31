package ru.radiationx.anilibria.ui.adapters.global

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.adapters.CommentRouteListItem
import ru.radiationx.anilibria.ui.adapters.ListItem

/**
 * Created by radiationx on 21.01.18.
 */
class CommentRouteDelegate : AdapterDelegate<MutableList<ListItem>>() {
    override fun isForViewType(items: MutableList<ListItem>, position: Int): Boolean = items[position] is CommentRouteListItem

    override fun onBindViewHolder(items: MutableList<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {}

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_comment_route, parent, false)
    )

    private inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

}