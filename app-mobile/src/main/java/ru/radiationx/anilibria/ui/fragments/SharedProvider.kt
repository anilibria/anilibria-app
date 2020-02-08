package ru.radiationx.anilibria.ui.fragments

import android.view.View

/**
 * Created by radiationx on 23.12.17.
 */
interface SharedProvider {
    val sharedViewLocal: View?
    fun getSharedView(): View?
}