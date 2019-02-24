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
import ru.radiationx.anilibria.ui.widgets.bbwidgets.BbView
import ru.radiationx.anilibria.utils.bbparser.BbParser

/**
 * Created by radiationx on 18.01.18.
 */
class CommentDelegate(
        private val listener: Listener
) : AdapterDelegate<MutableList<ListItem>>() {

    private val parser = BbParser()
    private val expandedComments = mutableSetOf<Int>()

    override fun isForViewType(items: MutableList<ListItem>, position: Int): Boolean = items[position] is CommentListItem

    override fun onBindViewHolder(items: MutableList<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val item = items[position] as CommentListItem
        (holder as ViewHolder).bind(item.item, expandedComments.contains(item.item.id))
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false),
            listener
    )

    private inner class ViewHolder(
            private val view: View,
            private val listener: Listener
    ) : RecyclerView.ViewHolder(view) {

        private lateinit var currentItem: Comment

        init {
            view.setOnClickListener { listener.onClick(currentItem) }
            view.item_content.setOverflowListener(object : BbView.OverflowListener {
                override fun onOverflowChanged(isOverFlow: Boolean) {
                    updateShowFull()
                }
            })
            view.item_comment_showfull.setOnClickListener {
                val newIsExpand = !view.item_content.isExpanded()
                if (newIsExpand) {
                    expandedComments.add(currentItem.id)
                } else {
                    expandedComments.remove(currentItem.id)
                }
                view.item_content.setExpand(newIsExpand)
            }
        }

        fun bind(item: Comment, isExpand: Boolean) {
            currentItem = item
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
                item_content.setExpand(isExpand)
            }

            //val parentHeight = (view.parent as ViewGroup).height
            val targetHeight = (240).toInt()
            view.item_content.setMaxHeightDp(targetHeight)
        }

        private fun updateShowFull() {
            val isOverflow = view.item_content.isOverflow()
            val isExpanded = view.item_content.isExpanded()
            view.item_comment_showfull.apply {
                visibility = if (isOverflow || isExpanded) View.VISIBLE else View.GONE
                text = if (isExpanded) {
                    "Свернуть"
                } else {
                    "Показать полностью"
                }
            }
            view.item_comment_content_shadow.apply {
                visibility = if (isOverflow && !isExpanded) View.VISIBLE else View.GONE
            }
        }
    }

    interface Listener {
        fun onClick(item: Comment)
    }
}