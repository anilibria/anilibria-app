package ru.radiationx.anilibria.ui.common.adapters

import android.util.SparseIntArray
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import com.hannesdorfmann.adapterdelegates3.AdapterDelegatesManager

class OptimizeDelegateManager<T> : AdapterDelegatesManager<T>() {

    private val sizes = SparseIntArray()

    override fun addDelegate(viewType: Int, allowReplacingDelegate: Boolean, delegate: AdapterDelegate<T>): AdapterDelegatesManager<T> {
        val result = super.addDelegate(viewType, allowReplacingDelegate, delegate)
        (delegate as? OptimizeDelegate)?.also {
            val poolSize = it.getPoolSize()
            if (poolSize != -1) {
                sizes.put(viewType, poolSize)
            }
        }
        return result
    }

    fun getPoolSizes(): List<Pair<Int, Int>> = (0 until sizes.size()).map { index ->
        Pair(sizes.keyAt(index), sizes.valueAt(index))
    }
}