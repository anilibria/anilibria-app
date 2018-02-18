package ru.radiationx.anilibria.model.data.holders

/**
 * Created by radiationx on 03.02.18.
 */
interface PreferencesHolder {
    fun getReleaseRemind(): Boolean
    fun setReleaseRemind(state: Boolean)
    fun getEpisodesIsReverse(): Boolean
}