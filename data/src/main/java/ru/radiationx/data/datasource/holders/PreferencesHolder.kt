package ru.radiationx.data.datasource.holders

import io.reactivex.Observable

/**
 * Created by radiationx on 03.02.18.
 */
interface PreferencesHolder {
    companion object {
        const val QUALITY_NO = -1
        const val QUALITY_SD = 0
        const val QUALITY_HD = 1
        const val QUALITY_ALWAYS = 2
        const val QUALITY_FULL_HD = 3

        const val PLAYER_TYPE_NO = -1
        const val PLAYER_TYPE_EXTERNAL = 0
        const val PLAYER_TYPE_INTERNAL = 1
        const val PLAYER_TYPE_ALWAYS = 2

        const val PIP_BUTTON = 0
        const val PIP_AUTO = 1
    }

    fun getReleaseRemind(): Boolean
    fun setReleaseRemind(state: Boolean)

    fun getSearchRemind(): Boolean
    fun setSearchRemind(state: Boolean)

    fun getEpisodesIsReverse(): Boolean

    fun getQuality(): Int
    fun setQuality(value: Int)

    fun getPlayerType(): Int
    fun setPlayerType(value: Int)

    var playSpeed: Float

    var pipControl: Int


    var notificationsAll: Boolean
    fun observeNotificationsAll(): Observable<Boolean>

    var notificationsService: Boolean
    fun observeNotificationsService(): Observable<Boolean>

}