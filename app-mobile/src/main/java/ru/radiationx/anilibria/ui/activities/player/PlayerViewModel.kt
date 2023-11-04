package ru.radiationx.anilibria.ui.activities.player

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.radiationx.data.datasource.holders.EpisodesCheckerHolder
import ru.radiationx.data.entity.domain.release.Episode
import ru.radiationx.data.entity.domain.release.EpisodeAccess
import ru.radiationx.data.entity.domain.release.PlayerSkips
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.quill.QuillExtra
import ru.radiationx.shared.ktx.coRunCatching
import toothpick.InjectConstructor
import java.util.concurrent.TimeUnit

data class PlayerExtra(
    val episodeId: EpisodeId,
) : QuillExtra

enum class PlayerQuality {
    SD,
    HD,
    FULLHD
}

@InjectConstructor
class PlayerViewModel(
    private val argExtra: PlayerExtra,
    private val releaseInteractor: ReleaseInteractor,
    private val episodesCheckerHolder: EpisodesCheckerHolder,
) : ViewModel() {

    private val _targetQuality = MutableStateFlow(PlayerQuality.SD)
    val targetQuality = _targetQuality.asStateFlow()

    private val _currentSpeed = MutableStateFlow(1.0f)
    val currentSpeed = _currentSpeed.asStateFlow()

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
                val release = _dataState.value.data ?: return@onEach
                val episodeStates = release.episodes.map {
                    it.toState(quality)
                }
                val action = PlayerAction.PlaylistChange(episodeStates)
                _actions.emit(action)
            }
            .launchIn(viewModelScope)
    }

    fun playEpisode(episodeId: EpisodeId, quality: PlayerQuality?, speed: Float?) {
        Log.e("kekeke", "playEpisode $episodeId, $quality, $speed")
        _episodeId.value = episodeId
        if (quality != null) {
            _targetQuality.value = quality
        }
        if (speed != null) {
            _currentSpeed.value = speed
        }
        loadData(episodeId)
    }

    fun refresh() {
        loadData(_episodeId.value)
    }

    fun onSettingsClick() {
        val episode = _dataState.value.data?.episodes?.find { it.id == _episodeId.value } ?: return
        val settingsState = PlayerSettingsState(
            currentSpeed = _currentSpeed.value,
            currentQuality = episode.getActualQuality(_targetQuality.value) ?: PlayerQuality.SD,
            availableQualities = episode.getAvailableQualities()
        )
        viewModelScope.launch {
            _actions.emit(PlayerAction.ShowSettings(settingsState))
        }
    }

    fun onPlaylistClick() {

    }

    fun onQualitySelected(quality: PlayerQuality) {
        _targetQuality.value = quality
    }

    fun onSpeedSelected(speed: Float) {
        _currentSpeed.value = speed
    }

    fun saveEpisodeSeek(episodeId: EpisodeId, seek: Long) {
        viewModelScope.launch {
            episodesCheckerHolder.putEpisode(
                EpisodeAccess(
                    id = episodeId,
                    seek = seek,
                    isViewed = true,
                    lastAccess = System.currentTimeMillis()
                )
            )
        }
    }

    fun onEpisodeChanged(episodeId: EpisodeId, duration: Long) {
        _episodeId.value = episodeId
        viewModelScope.launch {
            val access = episodesCheckerHolder.getEpisodes(episodeId.releaseId).find {
                it.id == episodeId
            }
            val accessSeek = access?.seek
            if ((accessSeek ?: 0) >= duration - TimeUnit.SECONDS.toMillis(10)) {
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
                    "Loaded release is null"
                }
            }.onSuccess { release ->
                _dataState.update { it.copy(data = release) }
                val quality = _targetQuality.value
                val access = episodesCheckerHolder.getEpisodes(episodeId.releaseId).find {
                    it.id == episodeId
                }
                val episodeStates = release.episodes.map {
                    it.toState(quality)
                }
                val action = PlayerAction.InitialPlay(episodeStates, episodeId, access?.seek ?: 0)
                _actions.emit(action)
            }.onFailure { error ->
                _dataState.update { it.copy(error = error) }
            }
            _dataState.update { it.copy(loading = false) }
        }
    }

    fun Release.toDataState(episodeId: EpisodeId) = PlayerDataState(
        id = id,
        title = (title ?: titleEng).orEmpty(),
        episodeTitle = episodes.find { it.id == episodeId }?.title.orEmpty()
    )

    fun Episode.toState(quality: PlayerQuality) = EpisodeState(
        id = id,
        title = title.orEmpty(),
        url = getUrlByQuality(quality),
        skips = skips
    )

    private fun Episode.getAvailableQualities(): List<PlayerQuality> = buildList {
        if (!urlSd.isNullOrEmpty()) {
            add(PlayerQuality.SD)
        }
        if (!urlHd.isNullOrEmpty()) {
            add(PlayerQuality.HD)
        }
        if (!urlFullHd.isNullOrEmpty()) {
            add(PlayerQuality.FULLHD)
        }
    }

    private fun Episode.getActualQuality(quality: PlayerQuality): PlayerQuality? {
        val available = getAvailableQualities()
        var actual: PlayerQuality? = quality

        if (actual == PlayerQuality.FULLHD && actual !in available) {
            actual = PlayerQuality.HD
        }
        if (actual == PlayerQuality.HD && actual !in available) {
            actual = PlayerQuality.SD
        }
        if (actual == PlayerQuality.SD && actual !in available) {
            actual = null
        }
        return actual
    }

    private fun Episode.getUrlByQuality(quality: PlayerQuality): String {
        val url = when (getActualQuality(quality)) {
            PlayerQuality.SD -> urlSd
            PlayerQuality.HD -> urlHd
            PlayerQuality.FULLHD -> urlFullHd
            null -> null
        }
        return requireNotNull(url) {
            "Can't find any url for episode with $quality"
        }
    }
}

data class LoadingState<T>(
    val loading: Boolean = false,
    val data: T? = null,
    val error: Throwable? = null,
)

data class PlayerDataState(
    val id: ReleaseId,
    val title: String,
    val episodeTitle: String,
)

data class EpisodeState(
    val id: EpisodeId,
    val title: String,
    val url: String,
    val skips: PlayerSkips?,
)

sealed class PlayerAction {
    data class InitialPlay(
        val episodes: List<EpisodeState>,
        val episodeId: EpisodeId,
        val seek: Long,
    ) : PlayerAction()

    data class PlaylistChange(
        val episodes: List<EpisodeState>,
    ) : PlayerAction()

    data class Play(
        val seek: Long?,
    ) : PlayerAction()

    data class ShowSettings(
        val state: PlayerSettingsState,
    ) : PlayerAction()
}