package ru.radiationx.anilibria.common

import android.annotation.SuppressLint
import androidx.leanback.widget.DiffCallback

object CardDiffCallback : DiffCallback<Any>() {

    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        if (oldItem is CardItem && newItem is CardItem) {
            return oldItem.getId() == newItem.getId()
        }
        return oldItem == newItem
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        return oldItem == newItem
    }
}