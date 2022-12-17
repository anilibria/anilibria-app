package ru.radiationx.anilibria.screen.player.speed

import androidx.lifecycle.MutableLiveData
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.data.interactors.ReleaseInteractor
import toothpick.InjectConstructor

@InjectConstructor
class PlayerSpeedViewModel(
    private val releaseInteractor: ReleaseInteractor,
    private val guidedRouter: GuidedRouter
) : LifecycleViewModel() {

    private val speedList = listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)

    val speedData = MutableLiveData<List<String>>()
    val selectedIndex = MutableLiveData<Int>()

    override fun onColdCreate() {
        super.onColdCreate()
        speedData.value = speedList.map {
            if (it == 1.0f) {
                "Обычная"
            } else {
                "${it}x"
            }
        }
        selectedIndex.value = speedList.indexOf(releaseInteractor.getPlaySpeed())
    }

    fun applySpeed(index: Int) {
        guidedRouter.close()
        releaseInteractor.setPlaySpeed(speedList[index])
    }
}