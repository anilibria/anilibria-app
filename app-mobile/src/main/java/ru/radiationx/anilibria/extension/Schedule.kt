package ru.radiationx.anilibria.extension

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.addItemsPositionListener(
    onScrolled: (first: Int, last: Int) -> Unit
): RecyclerView.OnScrollListener {

    val listener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                (recyclerView.layoutManager as? LinearLayoutManager)?.also {
                    onScrolled.invoke(
                        it.findFirstVisibleItemPosition(),
                        it.findLastVisibleItemPosition()
                    )
                }
            }
        }
    }
    this.addOnScrollListener(listener)
    return listener
}