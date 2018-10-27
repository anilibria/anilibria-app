package ru.radiationx.anilibria.ui.adapters.release.list

import android.graphics.Color
import android.os.Build
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.synthetic.main.item_release.view.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.ui.adapters.BaseItemListener
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseListItem
import ru.radiationx.anilibria.ui.fragments.release.details.ReleaseFragment

/**
 * Created by radiationx on 13.01.18.
 */
class ReleaseItemDelegate(private val itemListener: Listener) : AdapterDelegate<MutableList<ListItem>>() {
    override fun isForViewType(items: MutableList<ListItem>, position: Int): Boolean = items[position] is ReleaseListItem

    override fun onBindViewHolder(items: MutableList<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val item = items[position] as ReleaseListItem
        (holder as ViewHolder).bind(item.item, position)
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_release, parent, false)
    )

    private inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        private lateinit var currentItem: ReleaseItem

        init {
            itemView.run {
                setOnClickListener {
                    itemListener.onItemClick(layoutPosition, item_image)
                    itemListener.onItemClick(currentItem, layoutPosition)
                }
                setOnLongClickListener {
                    itemListener.onItemLongClick(currentItem)
                }
            }
        }

        fun bind(item: ReleaseItem, position: Int) {
            currentItem = item
            view.run {
                if (item.episodesCount == null) {
                    item_title.text = item.title
                } else {
                    item_title.text = String.format("%s (%s)", item.title, item.episodesCount)
                }
                item_desc.text = Html.fromHtml(item.description)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    //item_image.transitionName = ReleaseFragment.TRANSACTION + "_" + position
                    item_image.transitionName = "${ReleaseFragment.TRANSACTION}_${item.id}_${item.torrentLink}"
                }
                item_new_indicator.visibility = if(item.isNew) View.VISIBLE else View.GONE
                ImageLoader.getInstance().displayImage(item.image, item_image)
            }
        }
    }

    interface Listener : BaseItemListener<ReleaseItem> {
        fun onItemClick(position: Int, view: View)
    }
}