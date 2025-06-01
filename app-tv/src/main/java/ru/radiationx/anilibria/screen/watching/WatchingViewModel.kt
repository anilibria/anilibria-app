package ru.radiationx.anilibria.screen.watching

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import ru.radiationx.anilibria.common.BaseRowsViewModel
import ru.radiationx.data.api.auth.AuthRepository
import ru.radiationx.data.api.auth.models.AuthState
import ru.radiationx.data.app.episodeaccess.EpisodesCheckerHolder
import ru.radiationx.data.app.history.HistoryRepository
import javax.inject.Inject

class WatchingViewModel @Inject constructor(
    authRepository: AuthRepository,
    historyRepository: HistoryRepository,
    episodesCheckerHolder: EpisodesCheckerHolder,
) : BaseRowsViewModel() {

    companion object {
        const val HISTORY_ROW_ID = 1L
        const val CONTINUE_ROW_ID = 2L
        const val FAVORITES_ROW_ID = 3L
        const val RECOMMENDS_ROW_ID = 4L
    }

    override val rowIds: List<Long> =
        listOf(CONTINUE_ROW_ID, HISTORY_ROW_ID, FAVORITES_ROW_ID, RECOMMENDS_ROW_ID)

    override val availableRows: MutableSet<Long> =
        mutableSetOf(CONTINUE_ROW_ID, HISTORY_ROW_ID, RECOMMENDS_ROW_ID)

    init {
        combine(
            episodesCheckerHolder.observeEpisodes().map { it.isNotEmpty() },
            historyRepository.observeReleases().map { it.items.isNotEmpty() },
            authRepository.observeAuthState().map { it == AuthState.AUTH }
        ) { hasContinue, hasHistory, hasAuth ->
            updateAvailableRow(CONTINUE_ROW_ID, hasContinue)
            updateAvailableRow(HISTORY_ROW_ID, hasHistory)
            updateAvailableRow(FAVORITES_ROW_ID, hasAuth)
        }.launchIn(viewModelScope)
    }
}