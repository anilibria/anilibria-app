package ru.radiationx.anilibria.screen.player.quality

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.anilibria.screen.player.PlayerExtra
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.interactors.ReleaseInteractor
import toothpick.InjectConstructor

@InjectConstructor
class PlayerQualityViewModel(
    private val argExtra: PlayerExtra,
    private val releaseInteractor: ReleaseInteractor,
    private val guidedRouter: GuidedRouter
) : LifecycleViewModel() {

    companion object {
        const val SD_ACTION_ID = PreferencesHolder.QUALITY_SD.toLong()
        const val HD_ACTION_ID = PreferencesHolder.QUALITY_HD.toLong()
        const val FULL_HD_ACTION_ID = PreferencesHolder.QUALITY_FULL_HD.toLong()
    }

    val availableData = MutableLiveData<List<Long>>()
    val selectedData = MutableLiveData<Long>()

    override fun onCreate() {
        super.onCreate()

        updateAvailable()

        releaseInteractor
            .observeQuality()
            .onEach {
                update(it)
            }
            .launchIn(viewModelScope)
    }

    fun applyQuality(quality: Long) {
        guidedRouter.close()
        val value = when (quality) {
            SD_ACTION_ID -> PreferencesHolder.QUALITY_SD
            HD_ACTION_ID -> PreferencesHolder.QUALITY_HD
            FULL_HD_ACTION_ID -> PreferencesHolder.QUALITY_FULL_HD
            else -> PreferencesHolder.QUALITY_SD
        }
        releaseInteractor.setQuality(value)
    }

    private fun updateAvailable() {
        val available = mutableListOf(SD_ACTION_ID, HD_ACTION_ID, FULL_HD_ACTION_ID)
        releaseInteractor.getFull(argExtra.releaseId)
            ?.episodes
            ?.firstOrNull { it.id == argExtra.episodeId }
            ?.also {
                if (it.urlFullHd.isNullOrEmpty()) {
                    available.remove(FULL_HD_ACTION_ID)
                }
                if (it.urlHd.isNullOrEmpty()) {
                    available.remove(HD_ACTION_ID)
                }
                if (it.urlSd.isNullOrEmpty()) {
                    available.remove(SD_ACTION_ID)
                }
            }
        availableData.value = available
    }

    private fun update(currentQuality: Int) {
        val available = availableData.value!!
        var selectedAction = when (currentQuality) {
            PreferencesHolder.QUALITY_SD -> SD_ACTION_ID
            PreferencesHolder.QUALITY_HD -> HD_ACTION_ID
            PreferencesHolder.QUALITY_FULL_HD -> FULL_HD_ACTION_ID
            else -> SD_ACTION_ID
        }

        if (selectedAction == FULL_HD_ACTION_ID && !available.contains(selectedAction)) {
            selectedAction = HD_ACTION_ID
        }
        if (selectedAction == HD_ACTION_ID && !available.contains(selectedAction)) {
            selectedAction = SD_ACTION_ID
        }
        if (selectedAction == SD_ACTION_ID && !available.contains(selectedAction)) {
            selectedAction = -1L
        }

        selectedData.value = selectedAction
    }
}