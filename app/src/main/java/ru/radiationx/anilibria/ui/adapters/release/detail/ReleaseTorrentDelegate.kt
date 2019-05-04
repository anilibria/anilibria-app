package ru.radiationx.anilibria.ui.adapters.release.detail

import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_release_torrent.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.release.TorrentItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseTorrentListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeDelegate
import java.text.DecimalFormat
import java.util.*

/**
 * Created by radiationx on 13.01.18.
 */
class ReleaseTorrentDelegate() : AppAdapterDelegate<ReleaseTorrentListItem, ListItem, ReleaseTorrentDelegate.ViewHolder>(
        R.layout.item_release_torrent,
        { it is ReleaseTorrentListItem },
        { ViewHolder(it) }
), OptimizeDelegate {

    override fun getPoolSize(): Int = 20

    override fun bindData(item: ReleaseTorrentListItem, holder: ViewHolder) =
            holder.bind(item.item)

    class ViewHolder(
            override val containerView: View
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        private lateinit var currentItem: TorrentItem

        init {

        }

        fun bind(item: TorrentItem) {
            currentItem = item
            itemTorrentTitle.text = "Серия ${item.series}"
            itemTorrentDesc.text = item.quality
            itemTorrentSize.text = readableFileSize(item.size)
            itemTorrentSeeders.text = item.seeders.toString()
            itemTorrentLeechers.text = item.leechers.toString()
            itemTorrentDate.text = Date().toString()
        }

        private fun readableFileSize(size: Long): String {
            if (size <= 0) return "0"
            val units = arrayOf("B", "kB", "MB", "GB", "TB")
            val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
            return DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
        }

    }

}