package ru.radiationx.anilibria.screen.player.speed

import kotlinx.coroutines.flow.MutableStateFlow
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.data.datasource.holders.PreferencesHolder
import javax.inject.Inject

class PlayerSpeedViewModel @Inject constructor(
    private val preferencesHolder: PreferencesHolder,
    private val guidedRouter: GuidedRouter,
) : LifecycleViewModel() {

    private val speedList = listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)

    val speedData = MutableStateFlow<List<String>>(emptyList())
    val selectedIndex = MutableStateFlow<Int?>(null)

    init {
        speedData.value = speedList.map {
            if (it == 1.0f) {
                "Обычная"
            } else {
                "${it}x"
            }
        }
        selectedIndex.value = speedList.indexOf(preferencesHolder.playSpeed.value)
    }

    fun applySpeed(index: Int) {
        guidedRouter.close()
        preferencesHolder.playSpeed.value = speedList[index]
    }
}