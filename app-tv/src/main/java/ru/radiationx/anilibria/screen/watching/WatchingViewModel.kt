package ru.radiationx.anilibria.screen.watching

import io.reactivex.android.schedulers.AndroidSchedulers
import ru.radiationx.anilibria.common.BaseRowsViewModel
import ru.radiationx.data.datasource.holders.EpisodesCheckerHolder
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.data.repository.HistoryRepository
import toothpick.InjectConstructor

@InjectConstructor
class WatchingViewModel(
    private val authRepository: AuthRepository,
    private val historyRepository: HistoryRepository,
    private val episodesCheckerHolder: EpisodesCheckerHolder
) : BaseRowsViewModel() {

    companion object {
        const val HISTORY_ROW_ID = 1L
        const val CONTINUE_ROW_ID = 2L
        const val FAVORITES_ROW_ID = 3L
        const val RECOMMENDS_ROW_ID = 4L
    }

    override val rowIds: List<Long> = listOf(CONTINUE_ROW_ID, HISTORY_ROW_ID, FAVORITES_ROW_ID, RECOMMENDS_ROW_ID)

    override val availableRows: MutableList<Long> = mutableListOf(CONTINUE_ROW_ID, HISTORY_ROW_ID, RECOMMENDS_ROW_ID)

    override fun onCreate() {
        super.onCreate()

        episodesCheckerHolder
            .observeEpisodes()
            .observeOn(AndroidSchedulers.mainThread())
            .lifeSubscribe {
                updateAvailableRow(CONTINUE_ROW_ID, it.isNotEmpty())
            }

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