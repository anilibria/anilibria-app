package ru.radiationx.data.app.preferences

import kotlinx.coroutines.flow.StateFlow
import ru.radiationx.data.api.releases.models.PlayerQuality
import ru.radiationx.data.player.PlayerTransport
import ru.radiationx.shared.ktx.android.PreferenceFlow

/**
 * Created by radiationx on 03.02.18.
 */
interface PreferencesHolder {

    val newDonationRemind: PreferenceFlow<Boolean>

    val releaseRemind: PreferenceFlow<Boolean>

    val episodesIsReverse: PreferenceFlow<Boolean>

    val playerTransport: PreferenceFlow<PlayerTransport>

    val playerQuality: PreferenceFlow<PlayerQuality>

    val playSpeed: PreferenceFlow<Float>

    val playerSkips: PreferenceFlow<Boolean>

    val playerSkipsTimer: PreferenceFlow<Boolean>

    val playerInactiveTimer: PreferenceFlow<Boolean>

    val playerAutoplay: PreferenceFlow<Boolean>

    val notificationsAll: PreferenceFlow<Boolean>

    val notificationsService: PreferenceFlow<Boolean>

    val availableSpeeds: StateFlow<List<Float>>

}

