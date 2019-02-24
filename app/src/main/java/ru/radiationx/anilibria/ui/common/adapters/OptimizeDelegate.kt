package ru.radiationx.anilibria.ui.common.adapters

import com.hannesdorfmann.adapterdelegates3.AdapterDelegate

interface OptimizeDelegate {
    open fun getPoolSize(): Int = -1
}