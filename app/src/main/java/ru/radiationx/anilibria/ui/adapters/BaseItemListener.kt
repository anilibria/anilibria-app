package ru.radiationx.anilibria.ui.adapters

/**
 * Created by radiationx on 13.01.18.
 */
interface BaseItemListener<T> {
     fun onItemClick(item: T, position: Int)

     fun onItemLongClick(item: T): Boolean
}