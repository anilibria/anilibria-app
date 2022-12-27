package ru.radiationx.anilibria.screen.player.quality

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.anilibria.screen.player.PlayerExtra
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.entity.domain.release.Release
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

    val availableData = MutableStateFlow<List<Long>>(emptyList())
    val selectedData = MutableStateFlow<Long?>(null)

    init {
        combine(
            releaseInteractor.observeFull(argExtra.releaseId),
            releaseInteractor.observeQuality()
        ) { release, quality ->
            updateAvailable(release, quality)
        }.launchIn(viewModelScope)
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

    private fun updateAvailable(release: Release, quality: Int) {
        val episode = release.episodes.firstOrNull { it.id == argExtra.episodeId } ?: return
        val available = buildList<Long> {
            if (!episode.urlSd.isNullOrEmpty()) {
                add(SD_ACTION_ID)
            }
            if (!episode.urlHd.isNullOrEmpty()) {
                add(HD_ACTION_ID)
            }
            if (!episode.urlFullHd.isNullOrEmpty()) {
                add(FULL_HD_ACTION_ID)
            }
        }
        availableData.value = available
        selectedData.value = computeQualityActionId(available, quality)
    }

    private fun computeQualityActionId(available: List<Long>, currentQuality: Int): Long {
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

        return selectedAction
    }
}