package ru.radiationx.anilibria.ui.activities.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.ui.activities.player.controllers.PlayerSettingsState
import ru.radiationx.anilibria.ui.activities.player.di.SharedPlayerData
import ru.radiationx.combineflow.buildCombineFlow
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.entity.common.PlayerQuality
import javax.inject.Inject

class PlayerSettingViewModel @Inject constructor(
    private val sharedPlayerData: SharedPlayerData,
    private val preferencesHolder: PreferencesHolder,
) : ViewModel() {

    val settingsState = buildCombineFlow {
        val qualityInfo by sharedPlayerData.dataState
            .flatMapLatest { data ->
                sharedPlayerData.episodeId.map {
                    data.data?.getEpisode(it)?.qualityInfo
                }
            }
            .distinctUntilChanged()
            .register()
        val playSpeed by preferencesHolder.playSpeed.register()
        val playerQuality by preferencesHolder.playerQuality.register()
        val playerSkips by preferencesHolder.playerSkips.register()
        val playerSkipsTimer by preferencesHolder.playerSkipsTimer.register()
        val playerInactiveTime by preferencesHolder.playerInactiveTimer.register()
        val playerAutoplay by preferencesHolder.playerAutoplay.register()
        collect {
            val currentQuality = qualityInfo?.getActualFor(playerQuality) ?: playerQuality
            val availableQualities = qualityInfo?.available ?: PlayerQuality.entries.toSet()
            PlayerSettingsState(
                currentSpeed = playSpeed,
                currentQuality = currentQuality,
                availableQualities = availableQualities,
                skipsEnabled = playerSkips,
                skipsTimerEnabled = playerSkipsTimer,
                inactiveTimerEnabled = playerInactiveTime,
                autoplayEnabled = playerAutoplay
            )
        }
    }

    private val _showAction = MutableSharedFlow<Unit>()
    val actions = _showAction.asSharedFlow()

    fun onSettingsClick() {
        viewModelScope.launch {
            _showAction.emit(Unit)
        }
    }


    fun onQualitySelected(quality: PlayerQuality) {
        preferencesHolder.playerQuality.value = quality
    }

    fun onSpeedSelected(speed: Float) {
        preferencesHolder.playSpeed.value = speed
    }

    fun onSkipsEnabledSelected(state: Boolean) {
        preferencesHolder.playerSkips.value = state
    }

    fun onSkipsTimerEnabledChange(state: Boolean) {
        preferencesHolder.playerSkipsTimer.value = state
    }

    fun onInactiveTimerEnabledChange(state: Boolean) {
        preferencesHolder.playerInactiveTimer.value = state
    }

    fun onAutoplayEnabledChange(state: Boolean) {
        preferencesHolder.playerAutoplay.value = state
    }

}

