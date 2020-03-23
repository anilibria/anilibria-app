package ru.radiationx.anilibria.screen.watching

import io.reactivex.android.schedulers.AndroidSchedulers
import ru.radiationx.anilibria.common.BaseRowsViewModel
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.data.repository.HistoryRepository
import toothpick.InjectConstructor

@InjectConstructor
class WatchingViewModel(
    private val authRepository: AuthRepository,
    private val historyRepository: HistoryRepository
) : BaseRowsViewModel() {

    companion object {
        const val HISTORY_ROW_ID = 1L
        const val CONTINUE_ROW_ID = 2L
        const val FAVORITES_ROW_ID = 3L
        const val RECOMMENDS_ROW_ID = 4L
    }

    override val rowIds: List<Long> = listOf(HISTORY_ROW_ID, CONTINUE_ROW_ID, FAVORITES_ROW_ID, RECOMMENDS_ROW_ID)

    override val availableRows: MutableList<Long> = mutableListOf(HISTORY_ROW_ID, RECOMMENDS_ROW_ID)

    override fun onCreate() {
        super.onCreate()

        historyRepository
            .observeReleases()
            .observeOn(AndroidSchedulers.mainThread())
            .lifeSubscribe {
                updateAvailableRow(HISTORY_ROW_ID, it.isNotEmpty())
            }

        authRepository
            .observeUser()
            .observeOn(AndroidSchedulers.mainThread())
            .lifeSubscribe {
                updateAvailableRow(FAVORITES_ROW_ID, it.authState == AuthState.AUTH)
            }
    }
}