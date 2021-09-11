package ru.radiationx.anilibria.ui.common.adapters

import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter
import ru.radiationx.anilibria.ui.adapters.IBundledViewHolder

open class OptimizeAdapter<T>(
    private val itemCallback: DiffUtil.ItemCallback<T>,
    private val manager: OptimizeDelegateManager<List<T>> = OptimizeDelegateManager()
) : AsyncListDifferDelegationAdapter<T>(itemCallback, manager) {

    private val bundleNestedStatesKey = "nested_states_${this.javaClass.simpleName}"
    private var states: SparseArray<Parcelable?> = SparseArray()

    private var currentRecyclerView: RecyclerView? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        currentRecyclerView = recyclerView
        recyclerView.recycledViewPool.apply {
            manager.getPoolSizes().forEach {
                setMaxRecycledViews(it.first, it.second)
            }
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        currentRecyclerView = null
        recyclerView.recycledViewPool.clear()
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        (holder as? IBundledViewHolder)?.apply {
            val state = holder.saveState()
            states.put(getStateId(), state)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any?>
    ) {
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

    fun addDelegate(delegate: AdapterDelegate<List<T>>) {
        manager.addDelegate(delegate)
    }

    fun addDelegate(viewType: Int, delegate: AdapterDelegate<List<T>>) {
        manager.addDelegate(viewType, delegate)
    }

    fun addDelegate(viewType: Int, allowReplacingDelegate: Boolean, delegate: AdapterDelegate<List<T>>) {
        manager.addDelegate(viewType, allowReplacingDelegate, delegate)
    }
}