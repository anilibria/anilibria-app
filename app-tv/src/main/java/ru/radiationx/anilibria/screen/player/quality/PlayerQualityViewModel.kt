package ru.radiationx.anilibria.screen.player.quality

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.anilibria.screen.player.PlayerExtra
import ru.radiationx.data.entity.common.PlayerQuality
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.interactors.ReleaseInteractor
import javax.inject.Inject

class PlayerQualityViewModel @Inject constructor(
    private val argExtra: PlayerExtra,
    private val releaseInteractor: ReleaseInteractor,
    private val qualityPreference: PlayerQualityPreference,
    private val guidedRouter: GuidedRouter,
) : LifecycleViewModel() {

    companion object {
        const val AUTO_ACTION_ID = -1L
        val SD_ACTION_ID = PlayerQuality.SD.ordinal.toLong()
        val HD_ACTION_ID = PlayerQuality.HD.ordinal.toLong()
        val FULL_HD_ACTION_ID = PlayerQuality.FULLHD.ordinal.toLong()
    }

    val availableData = MutableStateFlow<List<Long>>(emptyList())
    val selectedData = MutableStateFlow<Long?>(null)
    val automaticQualityData = MutableStateFlow<PlayerQuality?>(null)

    private val releaseQuality = qualityPreference.forRelease(argExtra.releaseId)

    init {
        combine(
            releaseInteractor.observeFull(argExtra.releaseId),
            releaseQuality
        ) { release, quality ->
            updateAvailable(release, quality)
        }.launchIn(viewModelScope)
    }

    fun applyQuality(quality: Long) {
        guidedRouter.close()
        val value = when (quality) {
            AUTO_ACTION_ID -> null
            SD_ACTION_ID -> PlayerQuality.SD
            HD_ACTION_ID -> PlayerQuality.HD
            FULL_HD_ACTION_ID -> PlayerQuality.FULLHD
            else -> return
        }
        releaseQuality.value = value
    }

    private fun updateAvailable(release: Release, quality: PlayerQuality?) {
        val episode = release.episodes.firstOrNull { it.id == argExtra.episodeId } ?: return
        automaticQualityData.value = qualityPreference.automaticQuality(episode.qualityInfo)
        availableData.value = listOf(AUTO_ACTION_ID) + episode.qualityInfo.available.map { it.ordinal.toLong() }
        selectedData.value = if (quality != null && quality in episode.qualityInfo.available) {
            quality.ordinal.toLong()
        } else {
            AUTO_ACTION_ID
        }
    }

}
