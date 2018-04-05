package ru.radiationx.anilibria.ui.adapters.global

import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.synthetic.main.item_comment.view.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.release.Comment
import ru.radiationx.anilibria.ui.adapters.CommentListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.utils.bbparser.BbParser

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

    val parser = BbParser()

    private inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: Comment) {
            view.run {
                item_nick.text = Html.fromHtml(item.authorNick)
                item_date.text = item.date
                comment_group_name.text = item.userGroupName

                val textColor = when (item.userGroup) {
                    1 -> R.color.userGroupColor_1
                    2 -> R.color.userGroupColor_2
                    3 -> R.color.userGroupColor_3
                    else -> R.color.userGroupColor_default
                }
                comment_group_name.setTextColor(ContextCompat.getColor(context, textColor))

                if (item.userGroup == 0) {
                    comment_group_icon.visibility = View.GONE
                } else {
                    comment_group_icon.visibility = View.VISIBLE
                    val iconColor = when (item.userGroup) {
                        1 -> R.color.userGroupColorIcon_1
                        2 -> R.color.userGroupColorIcon_2
                        3 -> R.color.userGroupColorIcon_3
                        else -> R.color.userGroupColorIcon_default
                    }
                    comment_group_icon.background?.setColorFilter(ContextCompat.getColor(context, iconColor), PorterDuff.Mode.SRC_ATOP)
                    comment_group_icon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_anilibria))
                    comment_group_icon.drawable?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
                }

                ImageLoader.getInstance().displayImage(item.avatar, comment_avatar)

                item.message?.let {
                    item_content.setContent(parser.parse(it).toSequence())
                }
            }
        }
    }


}