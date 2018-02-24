package ru.radiationx.anilibria.model.data.holders

/**
 * Created by radiationx on 03.02.18.
 */
interface PreferencesHolder {
    companion object {
        const val QUALITY_NO = -1
        const val QUALITY_SD = 0
        const val QUALITY_HD = 1
        const val QUALITY_ALWAYS = 2
    }

    fun getReleaseRemind(): Boolean
    fun setReleaseRemind(state: Boolean)
    fun getEpisodesIsReverse(): Boolean

    fun getQuality(): Int
    fun setQuality(value: Int)
}