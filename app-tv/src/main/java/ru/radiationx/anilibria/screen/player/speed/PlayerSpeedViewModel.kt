package ru.radiationx.anilibria.screen.player.speed

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.data.datasource.holders.PreferencesHolder
import javax.inject.Inject

class PlayerSpeedViewModel @Inject constructor(
    private val preferencesHolder: PreferencesHolder,
    private val guidedRouter: GuidedRouter,
) : LifecycleViewModel() {

    val speedState = MutableStateFlow(SpeedState())

    init {
        combine(
            preferencesHolder.availableSpeeds,
            preferencesHolder.playSpeed
        ) { speeds, speed ->
            SpeedState(
                speeds = speeds,
                selectedIndex = speeds.indexOf(speed)
            )
        }.onEach {
            speedState.value = it
        }.launchIn(viewModelScope)

    }

    fun applySpeed(index: Int) {
        guidedRouter.close()
        preferencesHolder.playSpeed.value = speedState.value.speeds[index]
    }

    data class SpeedState(
        val speeds: List<Float> = emptyList(),
        val selectedIndex: Int? = null,
    )
}