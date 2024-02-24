package ru.radiationx.anilibria.screen.player.quality

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.anilibria.screen.player.PlayerExtra
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.entity.common.PlayerQuality
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.interactors.ReleaseInteractor
import toothpick.InjectConstructor

@InjectConstructor
class PlayerQualityViewModel(
    private val argExtra: PlayerExtra,
    private val releaseInteractor: ReleaseInteractor,
    private val guidedRouter: GuidedRouter,
) : LifecycleViewModel() {

    companion object {
        val SD_ACTION_ID = PlayerQuality.SD.ordinal.toLong()
        val HD_ACTION_ID = PlayerQuality.HD.ordinal.toLong()
        val FULL_HD_ACTION_ID = PlayerQuality.FULLHD.ordinal.toLong()
    }

    val availableData = MutableStateFlow<List<Long>>(emptyList())
    val selectedData = MutableStateFlow<Long?>(null)

    init {
        combine(
            releaseInteractor.observeFull(argExtra.releaseId),
            releaseInteractor.observePlayerQuality()
        ) { release, quality ->
            updateAvailable(release, quality)
        }.launchIn(viewModelScope)
    }

    fun applyQuality(quality: Long) {
        guidedRouter.close()
        val value = when (quality) {
            SD_ACTION_ID -> PlayerQuality.SD
            HD_ACTION_ID -> PlayerQuality.HD
            FULL_HD_ACTION_ID -> PlayerQuality.FULLHD
            else -> PlayerQuality.SD
        }
        releaseInteractor.setPlayerQuality(value)
    }

    private fun updateAvailable(release: Release, quality: PlayerQuality) {
        val episode = release.episodes.firstOrNull { it.id == argExtra.episodeId } ?: return
        availableData.value = episode.qualityInfo.available.map { it.ordinal.toLong() }
        selectedData.value = episode.qualityInfo.getActualFor(quality)?.ordinal?.toLong() ?: -1L
    }

}