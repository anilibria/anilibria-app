package ru.radiationx.anilibria.ui.common.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.Space
import androidx.recyclerview.widget.RecyclerView
import ru.radiationx.anilibria.ui.adapters.ListItem

/*
* Fixes recyclerview scroll anchor https://gist.github.com/osipxd/45357cc939844b0994c85e11ffa0187a
* */
data class AnchorListItem(val id: Any = "top") : ListItem(id)

class AnchorDelegate : AppAdapterDelegate<AnchorListItem, ListItem, AnchorDelegate.ViewHolder>(
    viewChecker = { it is AnchorListItem },
) {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = Space(parent.context)
        view.layoutParams = RecyclerView.LayoutParams(
            /* width = */ ViewGroup.LayoutParams.MATCH_PARENT,
            /* height = */ 1
        )
        return ViewHolder(view)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}