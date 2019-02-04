package ru.radiationx.anilibria.ui.common.adapters

import com.hannesdorfmann.adapterdelegates3.AdapterDelegate

abstract class OptimizeDelegate<T> : AdapterDelegate<T>() {
    open fun getPoolSize(): Int = -1
}