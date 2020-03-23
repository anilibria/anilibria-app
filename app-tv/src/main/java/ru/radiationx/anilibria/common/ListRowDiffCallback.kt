package ru.radiationx.anilibria.common

import android.annotation.SuppressLint
import android.util.Log
import androidx.leanback.widget.DiffCallback
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.Row

object ListRowDiffCallback : DiffCallback<Any>() {

    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        oldItem as ListRow
        newItem as ListRow
        return oldItem.id == newItem.id
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        oldItem as ListRow
        newItem as ListRow
        return oldItem.adapter == newItem.adapter && oldItem.headerItem == newItem.headerItem
    }
}