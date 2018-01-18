package ru.radiationx.anilibria.ui.adapters.release.detail

import android.support.v7.widget.RecyclerView
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import kotlinx.android.synthetic.main.item_comment.view.*
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.release.Comment
import ru.radiationx.anilibria.model.system.ApiUtils
import ru.radiationx.anilibria.ui.adapters.CommentListItem
import ru.radiationx.anilibria.ui.adapters.ListItem

/**
 * Created by radiationx on 18.01.18.
 */
class CommentDelegate : AdapterDelegate<MutableList<ListItem>>() {
    override fun isForViewType(items: MutableList<ListItem>, position: Int): Boolean = items[position] is CommentListItem

    override fun onBindViewHolder(items: MutableList<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val item = items[position] as CommentListItem
        (holder as ViewHolder).bind(item.item)
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
    )

    private inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: Comment) {
            view.run {
                item_nick.text = item.authorNick
                item_date.text = item.date
                item_content.text = Html.fromHtml(item.message)
                Log.e("SUKA", "Bind: "+item.message)
            }
        }
    }

}