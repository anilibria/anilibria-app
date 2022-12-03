package ru.radiationx.anilibria.common

import android.annotation.SuppressLint
import androidx.leanback.widget.DiffCallback
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.Row

object RowDiffCallback : DiffCallback<Any>() {

    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        oldItem as Row
        newItem as Row
        return oldItem.id == newItem.id
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        oldItem as Row
        newItem as Row
        if (oldItem is ListRow && newItem is ListRow) {
            return oldItem.adapter == newItem.adapter && oldItem.headerItem == newItem.headerItem
        }
        return oldItem.headerItem == newItem.headerItem
    }
}