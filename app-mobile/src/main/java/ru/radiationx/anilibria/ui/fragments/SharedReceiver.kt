package ru.radiationx.anilibria.ui.fragments

/**
 * Created by radiationx on 23.12.17.
 */
interface SharedReceiver {
    var transitionNameLocal: String
    fun setTransitionName(name: String)
}