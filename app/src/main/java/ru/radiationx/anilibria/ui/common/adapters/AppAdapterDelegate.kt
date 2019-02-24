package ru.radiationx.anilibria.ui.common.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AbsListItemAdapterDelegate

abstract class AppAdapterDelegate<I : T, T, VH : RecyclerView.ViewHolder>(
        private val layoutRes: Int,
        private val viewChecker: (T) -> Boolean,
        private val holderCreator: (View) -> VH
) : AbsListItemAdapterDelegate<I, T, VH>() {

    open fun bindData(item: I, holder: VH) {}

    open fun applyPayloads(item: I, payloads: MutableList<Any>, holder: RecyclerView.ViewHolder) {}

    @Deprecated("No use please")
    override fun isForViewType(item: T, items: MutableList<T>, position: Int): Boolean {
        return viewChecker(item)
    }

    @Deprecated("No use please")
    override fun onBindViewHolder(item: I, viewHolder: VH, payloads: MutableList<Any>) {
        bindData(item, viewHolder)
        if (payloads.isNotEmpty()) {
            applyPayloads(item, payloads, viewHolder)
        }
    }

    @Deprecated("No use please")
    override fun onCreateViewHolder(parent: ViewGroup): VH {
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return holderCreator(view)
    }
}