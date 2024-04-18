package ru.radiationx.anilibria.ui.activities.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.ui.activities.player.controllers.PlayerSettingsState
import ru.radiationx.anilibria.ui.activities.player.di.SharedPlayerData
import ru.radiationx.anilibria.ui.activities.player.mappers.toDataState
import ru.radiationx.anilibria.ui.activities.player.mappers.toPlayerRelease
import ru.radiationx.anilibria.ui.activities.player.mappers.toState
import ru.radiationx.anilibria.ui.activities.player.models.LoadingState
import ru.radiationx.anilibria.ui.activities.player.models.PlayerAction
import ru.radiationx.anilibria.ui.activities.player.models.PlayerData
import ru.radiationx.anilibria.ui.activities.player.models.PlayerDataState
import ru.radiationx.data.datasource.holders.EpisodesCheckerHolder
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.entity.common.PlayerQuality
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.ReleaseRepository
import ru.radiationx.shared.ktx.coRunCatching
import toothpick.InjectConstructor
import java.util.concurrent.TimeUnit

@InjectConstructor
class PlayerViewModel(
    private val sharedPlayerData: SharedPlayerData,
    private val releaseInteractor: ReleaseInteractor,
    private val releaseRepository: ReleaseRepository,
    private val episodesCheckerHolder: EpisodesCheckerHolder,
    private val preferencesHolder: PreferencesHolder,
) : ViewModel() {

    companion object {
        private val seekThreshold = TimeUnit.SECONDS.toMillis(10)
    }

    val currentSpeed = preferencesHolder.playSpeed

    val playerSkipsEnabled: StateFlow<Boolean> = preferencesHolder.playerSkips

    val playerSkipsTimerEnabled: StateFlow<Boolean> = preferencesHolder.playerSkipsTimer

    val inactiveTimerEnabled: StateFlow<Boolean> = preferencesHolder.playerInactiveTimer

    val autoplayEnabled: StateFlow<Boolean> = preferencesHolder.playerAutoplay

    private val _episodeId = sharedPlayerData.episodeId
    val episodeId = _episodeId.asStateFlow()

    private val _dataState = sharedPlayerData.dataState
    private var _dataJob: Job? = null

    private val _loadingState = MutableStateFlow(LoadingState<PlayerDataState>())
    val loadingState = _loadingState.asStateFlow()

    private val _actions = MutableSharedFlow<PlayerAction>()
    val actions = _actions.asSharedFlow()

    init {
        combine(
            _episodeId,
            _dataState
        ) { episodeId, dataState ->
            _loadingState.value = LoadingState(
                loading = dataState.loading,
                data = dataState.data?.toDataState(episodeId),
                error = dataState.error
            )
        }.launchIn(viewModelScope)

        preferencesHolder
            .playerQuality
            .drop(1)
            .onEach { quality ->
                withData { data ->
                    val episodeStates = data.episodes.map { it.toState(quality) }
                    val action = PlayerAction.PlaylistChange(episodeStates)
                    _actions.emit(action)
                }
            }
            .launchIn(viewModelScope)

        sharedPlayerData.onEpisodeSelected.onEach {
            playEpisode(it)
        }.launchIn(viewModelScope)
    }

    fun initialPlayEpisode(episodeId: EpisodeId) {
        loadData(episodeId)
    }

    fun refresh() {
        loadData(_episodeId.value)
    }

    fun onSettingsClick() {
        launchWithData { data ->
            val episode = data.getEpisode(_episodeId.value) ?: return@launchWithData
            val quality = preferencesHolder.playerQuality.value
            val settingsState = PlayerSettingsState(
                currentSpeed = preferencesHolder.playSpeed.value,
                currentQuality = episode.qualityInfo.getActualFor(quality) ?: PlayerQuality.SD,
                availableQualities = episode.qualityInfo.available,
                skipsEnabled = preferencesHolder.playerSkips.value,
                skipsTimerEnabled = preferencesHolder.playerSkipsTimer.value,
                inactiveTimerEnabled = preferencesHolder.playerInactiveTimer.value,
                autoplayEnabled = preferencesHolder.playerAutoplay.value
            )
            _actions.emit(PlayerAction.ShowSettings(settingsState))
        }
    }

    fun onPlaylistClick() {
        launchWithData {
            _actions.emit(PlayerAction.ShowPlaylist)
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

    @OptIn(DelicateCoroutinesApi::class)
    fun saveEpisodeSeek(episodeId: EpisodeId, seek: Long) {
        GlobalScope.launch {
            releaseInteractor.setAccessSeek(episodeId, seek)
        }
    }

    private fun playEpisode(episodeId: EpisodeId) {
        launchWithData { data ->
            val quality = preferencesHolder.playerQuality.value
            val access = episodesCheckerHolder.getEpisode(episodeId)
            val episodeStates = data.episodes.map { it.toState(quality) }
            val action = PlayerAction.PlayEpisode(episodeStates, episodeId, access?.seek ?: 0)
            _actions.emit(action)
        }
    }

    fun onEpisodeTransition(episodeId: EpisodeId, duration: Long) {
        _episodeId.value = episodeId
        viewModelScope.launch {
            val access = episodesCheckerHolder.getEpisode(episodeId)
            val accessSeek = access?.seek
            if ((accessSeek ?: 0) >= duration - seekThreshold) {
                _actions.emit(PlayerAction.Play(0))
            } else {
                _actions.emit(PlayerAction.Play(accessSeek))
            }
        }
    }

    private fun loadData(episodeId: EpisodeId) {
        _dataJob?.cancel()
        _dataJob = null
        _dataState.value = LoadingState()
        _episodeId.value = episodeId
        _dataJob = viewModelScope.launch {
            _dataState.update { LoadingState(loading = true) }
            coRunCatching {
                releaseInteractor
                    .loadWithFranchises(episodeId.releaseId)
                    .map { it.toPlayerRelease() }
            }.onSuccess { releases ->
                _dataState.update { it.copy(data = PlayerData(releases)) }
                playEpisode(episodeId)
            }.onFailure { error ->
                _dataState.update { it.copy(error = error) }
            }
            _dataState.update { it.copy(loading = false) }
        }
    }

    private inline fun withData(block: (PlayerData) -> Unit) {
        val data = _dataState.value.data ?: return
        block.invoke(data)
    }

    private inline fun launchWithData(crossinline block: suspend CoroutineScope.(PlayerData) -> Unit) {
        val data = _dataState.value.data ?: return
        viewModelScope.launch {
            block.invoke(this, data)
        }
    }

}

