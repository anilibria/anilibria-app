package ru.radiationx.anilibria.screen.main

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.common.BaseRowsViewModel
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.data.repository.AuthRepository
import toothpick.InjectConstructor

@InjectConstructor
class MainViewModel(
    private val authRepository: AuthRepository
) : BaseRowsViewModel() {

    companion object {
        const val FEED_ROW_ID = 1L
        const val SCHEDULE_ROW_ID = 2L
        const val FAVORITE_ROW_ID = 3L
        const val YOUTUBE_ROW_ID = 4L

    }

    override val rowIds: List<Long> =
        listOf(FEED_ROW_ID, FAVORITE_ROW_ID, SCHEDULE_ROW_ID, YOUTUBE_ROW_ID)

    override val availableRows: MutableSet<Long> =
        mutableSetOf(FEED_ROW_ID, SCHEDULE_ROW_ID, YOUTUBE_ROW_ID)

    override fun onCreate() {
        super.onCreate()

        authRepository
            .observeAuthState()
            .distinctUntilChanged()
            .onEach {
                updateAvailableRow(FAVORITE_ROW_ID, it == AuthState.AUTH)
            }
            .launchIn(viewModelScope)
    }
}