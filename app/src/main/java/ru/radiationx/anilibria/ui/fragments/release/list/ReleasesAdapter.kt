package ru.radiationx.anilibria.ui.fragments.release.list

import android.os.Build
import android.text.Html
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.nostra13.universalimageloader.core.ImageLoader
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.ui.adapters.BaseAdapter
import ru.radiationx.anilibria.ui.adapters.BaseViewHolder
import ru.radiationx.anilibria.ui.fragments.release.details.ReleaseFragment

/* Created by radiationx on 31.10.17. */

class ReleasesAdapter : BaseAdapter<ReleaseItem, BaseViewHolder<*>>() {
    private var listener: ItemListener? = null
    var endless: Boolean = false
        set(enable) {
            field = enable
            notifyDataSetChanged()
        }

    fun setListener(listener: ItemListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*>? {
        when (viewType) {
            RELEASE_LAYOUT -> return ReleaseItemHolder(inflateLayout(parent, R.layout.item_release))
            LOAD_MORE_LAYOUT -> return LoadMoreHolder(inflateLayout(parent, R.layout.item_load_more))
        }
        return null
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val itemType = getItemViewType(position)
        if (itemType == RELEASE_LAYOUT) {
            (holder as ReleaseItemHolder).bind(getItem(position), position)
        } else if (itemType == LOAD_MORE_LAYOUT) {
            holder.bind(position)
        }
    }

    override fun getItemCount(): Int {
        var count = super.getItemCount()
        if (endless && count > 0) {
            count++
        }
        return count
    }

    override fun getItemViewType(position: Int): Int {
        return if (endless && position == itemCount - 1) {
            LOAD_MORE_LAYOUT
        } else RELEASE_LAYOUT
    }

    fun insertMore(list: List<ReleaseItem>) {
        val prevItems = itemCount
        this.items.addAll(list)
        Log.d("SUKA", "insertMore $prevItems : $itemCount")
        notifyItemRangeInserted(prevItems, itemCount)
    }

    internal inner class ReleaseItemHolder(itemView: View) : BaseViewHolder<ReleaseItem>(itemView) {
        var image: ImageView = itemView.findViewById(R.id.item_image)
        var title: TextView = itemView.findViewById(R.id.item_title)
        var desc: TextView = itemView.findViewById(R.id.item_desc)

        init {
            itemView.setOnClickListener {
                listener?.onItemClick(layoutPosition, image)
                listener?.onItemClick(getItem(layoutPosition), layoutPosition)
            }
        }

        override fun bind(item: ReleaseItem, position: Int) {
            title.text = String.format("%s (%s)", item.title, item.episodesCount)
            desc.text = Html.fromHtml(item.description)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                image.transitionName = ReleaseFragment.TRANSACTION + "_" + position
            }
            ImageLoader.getInstance().displayImage(item.image, image)
        }
    }

    private inner class LoadMoreHolder internal constructor(itemView: View) : BaseViewHolder<ReleaseItem>(itemView) {
        private val container: LinearLayout = itemView.findViewById(R.id.nl_lm_container)
        private val btn: Button = itemView.findViewById(R.id.nl_lm_btn)

        init {
            btn.visibility = View.GONE
            container.visibility = View.VISIBLE
        }

        override fun bind(position: Int) {
            Log.d("SUKA", "BIND LOAD_MORE")
            listener?.onLoadMore()
        }
    }

    interface ItemListener : BaseAdapter.OnItemClickListener<ReleaseItem> {
        fun onLoadMore()
        fun onItemClick(position: Int, view: View)
    }

    companion object {
        private val RELEASE_LAYOUT = 1
        private val LOAD_MORE_LAYOUT = 2
    }
}
