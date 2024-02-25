package ru.radiationx.data.datasource.holders

import kotlinx.coroutines.flow.Flow
import ru.radiationx.data.entity.common.PlayerQuality

/**
 * Created by radiationx on 03.02.18.
 */
interface PreferencesHolder {

    fun observeNewDonationRemind(): Flow<Boolean>
    var newDonationRemind: Boolean

    fun observeReleaseRemind(): Flow<Boolean>
    var releaseRemind: Boolean

    fun observeSearchRemind(): Flow<Boolean>
    var searchRemind: Boolean

    fun observeEpisodesIsReverse(): Flow<Boolean>
    val episodesIsReverse: Boolean

    var playerQuality: PlayerQuality
    fun observePlayerQuality(): Flow<PlayerQuality>

    var playSpeed: Float
    fun observePlaySpeed(): Flow<Float>

    var playerSkips:Boolean

    var notificationsAll: Boolean
    fun observeNotificationsAll(): Flow<Boolean>

    var notificationsService: Boolean
    fun observeNotificationsService(): Flow<Boolean>

}