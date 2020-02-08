package ru.radiationx.anilibria.ui.common.adapters

import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.util.SparseArray
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import ru.radiationx.anilibria.ui.adapters.IBundledViewHolder

open class OptimizeAdapter<T : List<*>>(
        private val manager: OptimizeDelegateManager<T> = OptimizeDelegateManager()
) : ListDelegationAdapter<T>(manager) {

    private val bundleNestedStatesKey = "nested_states_${this.javaClass.simpleName}"
    private var states: SparseArray<Parcelable?> = SparseArray()

    private var currentRecyclerView: androidx.recyclerview.widget.RecyclerView? = null

    override fun onAttachedToRecyclerView(recyclerView: androidx.recyclerview.widget.RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        currentRecyclerView = recyclerView
        recyclerView.recycledViewPool.apply {
            manager.getPoolSizes().forEach {
                setMaxRecycledViews(it.first, it.second)
            }
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: androidx.recyclerview.widget.RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        currentRecyclerView = null
        recyclerView.recycledViewPool.clear()
    }

    override fun onViewRecycled(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        (holder as? IBundledViewHolder)?.apply {
            val state = holder.saveState()
            states.put(getStateId(), state)
        }
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any?>) {
        super.onBindViewHolder(holder, position, payloads)
        (holder as? IBundledViewHolder)?.apply {
            val state = states[getStateId()]
            holder.restoreState(state)
            states.remove(getStateId())
        }
    }

    private fun saveState() {
        (0 until itemCount).forEach { index ->
            val holder = currentRecyclerView?.findViewHolderForAdapterPosition(index)
            (holder as? IBundledViewHolder)?.apply {
                val state = holder.saveState()
                states.put(getStateId(), state)
            }
        }
    }

    fun saveState(outState: Bundle?) {
        saveState()
        outState?.putSparseParcelableArray(bundleNestedStatesKey, states)
    }

    fun restoreState(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) return
        savedInstanceState.getSparseParcelableArray<Parcelable?>(bundleNestedStatesKey)?.also {
            states = it
        }
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