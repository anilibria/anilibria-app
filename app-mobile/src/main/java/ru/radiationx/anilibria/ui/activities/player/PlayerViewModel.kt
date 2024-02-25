package ru.radiationx.anilibria.ui.activities.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.ui.activities.player.controllers.PlayerSettingsState
import ru.radiationx.anilibria.ui.activities.player.mappers.toDataState
import ru.radiationx.anilibria.ui.activities.player.mappers.toState
import ru.radiationx.anilibria.ui.activities.player.models.LoadingState
import ru.radiationx.anilibria.ui.activities.player.models.PlayerAction
import ru.radiationx.anilibria.ui.activities.player.models.PlayerDataState
import ru.radiationx.data.datasource.holders.EpisodesCheckerHolder
import ru.radiationx.data.entity.common.PlayerQuality
import ru.radiationx.data.entity.domain.release.EpisodeAccess
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.quill.QuillExtra
import ru.radiationx.shared.ktx.coRunCatching
import toothpick.InjectConstructor
import java.util.concurrent.TimeUnit

data class PlayerExtra(
    val episodeId: EpisodeId,
) : QuillExtra

@InjectConstructor
class PlayerViewModel(
    private val argExtra: PlayerExtra,
    private val releaseInteractor: ReleaseInteractor,
    private val episodesCheckerHolder: EpisodesCheckerHolder,
) : ViewModel() {

    companion object {
        private val seekThreshold = TimeUnit.SECONDS.toMillis(10)
    }

    private val _targetQuality = releaseInteractor
        .observePlayerQuality()
        .stateIn(viewModelScope, SharingStarted.Lazily, releaseInteractor.getPlayerQuality())

    private val _currentSpeed = releaseInteractor
        .observePlaySpeed()
        .stateIn(viewModelScope, SharingStarted.Lazily, releaseInteractor.getPlaySpeed())

    val currentSpeed = _currentSpeed

    private val _episodeId = MutableStateFlow(argExtra.episodeId)
    val episodeId = _episodeId.asStateFlow()

    private val _dataState = MutableStateFlow(LoadingState<Release>())

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

        _targetQuality
            .drop(1)
            .onEach { quality ->
                withData { data ->
                    val skipsEnabled = releaseInteractor.getPlayerSkips()
                    val episodeStates =
                        data.episodes.map { it.toState(quality, skipsEnabled) }.asReversed()
                    val action = PlayerAction.PlaylistChange(episodeStates)
                    _actions.emit(action)
                }
            }
            .launchIn(viewModelScope)
    }

    fun initialPlayEpisode(episodeId: EpisodeId) {
        _episodeId.value = episodeId
        loadData(episodeId)
    }

    fun refresh() {
        loadData(_episodeId.value)
    }

    fun onSettingsClick() {
        launchWithData { data ->
            val episode = data.episodes.find { it.id == _episodeId.value } ?: return@launchWithData
            val quality = _targetQuality.value
            val settingsState = PlayerSettingsState(
                currentSpeed = _currentSpeed.value,
                currentQuality = episode.qualityInfo.getActualFor(quality) ?: PlayerQuality.SD,
                availableQualities = episode.qualityInfo.available
            )
            _actions.emit(PlayerAction.ShowSettings(settingsState))
        }
    }

    fun onPlaylistClick() {
        launchWithData { data ->
            val action = PlayerAction.ShowPlaylist(data.episodes.asReversed(), episodeId.value)
            _actions.emit(action)
        }
    }

    fun onQualitySelected(quality: PlayerQuality) {
        releaseInteractor.setPlayerQuality(quality)
    }

    fun onSpeedSelected(speed: Float) {
        releaseInteractor.setPlaySpeed(speed)
    }

    fun saveEpisodeSeek(episodeId: EpisodeId, seek: Long) {
        GlobalScope.launch {
            val access = EpisodeAccess(
                id = episodeId,
                seek = seek,
                isViewed = true,
                lastAccess = System.currentTimeMillis()
            )
            episodesCheckerHolder.putEpisode(access)
        }
    }

    fun playEpisode(episodeId: EpisodeId) {
        launchWithData { data ->
            val quality = _targetQuality.value
            val access = episodesCheckerHolder.getEpisode(episodeId)
            val skipsEnabled = releaseInteractor.getPlayerSkips()
            val episodeStates = data.episodes.map { it.toState(quality, skipsEnabled) }.asReversed()
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
        viewModelScope.launch {
            _dataState.update { LoadingState(loading = true) }
            coRunCatching {
                requireNotNull(releaseInteractor.getFull(episodeId.releaseId)) {
                    "Loaded release is null for $episodeId"
                }
            }.onSuccess { release ->
                _dataState.update { it.copy(data = release) }
                playEpisode(episodeId)
            }.onFailure { error ->
                _dataState.update { it.copy(error = error) }
            }
            _dataState.update { it.copy(loading = false) }
        }
    }

    private inline fun withData(block: (Release) -> Unit) {
        val data = _dataState.value.data ?: return
        block.invoke(data)
    }

    private inline fun launchWithData(crossinline block: suspend CoroutineScope.(Release) -> Unit) {
        val data = _dataState.value.data ?: return
        viewModelScope.launch {
            block.invoke(this, data)
        }
    }

}

