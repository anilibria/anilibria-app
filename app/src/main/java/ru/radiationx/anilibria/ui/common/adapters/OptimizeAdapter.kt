package ru.radiationx.anilibria.ui.common.adapters

import android.support.v7.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import ru.radiationx.anilibria.ui.adapters.ListItem

open class OptimizeAdapter<T : List<*>>(
        private val manager: OptimizeDelegateManager<T> = OptimizeDelegateManager()
) : ListDelegationAdapter<T>(manager) {

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.recycledViewPool.apply {
            manager.getPoolSizes().forEach {
                setMaxRecycledViews(it.first, it.second)
            }
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        recyclerView.recycledViewPool.clear()
    }

    fun addDelegate(delegate: AdapterDelegate<T>) {
        manager.addDelegate(delegate)
    }

    fun addDelegate(viewType: Int, delegate: AdapterDelegate<T>) {
        manager.addDelegate(viewType, delegate)
    }

    fun addDelegate(viewType: Int, allowReplacingDelegate: Boolean, delegate: AdapterDelegate<T>) {
        manager.addDelegate(viewType, allowReplacingDelegate, delegate)
    }

}