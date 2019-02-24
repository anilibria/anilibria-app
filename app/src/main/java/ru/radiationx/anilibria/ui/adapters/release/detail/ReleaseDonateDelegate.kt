package ru.radiationx.anilibria.ui.adapters.release.detail

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseDonateListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeDelegate

/**
 * Created by radiationx on 21.01.18.
 */
class ReleaseDonateDelegate(
        private val itemListener: Listener
) : AppAdapterDelegate<ReleaseDonateListItem, ListItem, ReleaseDonateDelegate.ViewHolder>(
        R.layout.item_release_donate,
        { it is ReleaseDonateListItem },
        { ViewHolder(it, itemListener) }
) {

    class ViewHolder(
            val view: View,
            private val itemListener: Listener
    ) : RecyclerView.ViewHolder(view) {
        init {
            view.run {
                view.setOnClickListener {
                    itemListener.onClickDonate()
                }
            }
        }
    }

    interface Listener {
        fun onClickDonate()
    }
}