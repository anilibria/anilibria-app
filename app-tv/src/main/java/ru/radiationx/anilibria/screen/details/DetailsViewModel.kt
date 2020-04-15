package ru.radiationx.anilibria.screen.details

import android.util.Log
import ru.radiationx.anilibria.common.BaseRowsViewModel
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.data.repository.HistoryRepository
import toothpick.InjectConstructor

@InjectConstructor
class DetailsViewModel(
    private val releaseInteractor: ReleaseInteractor,
    private val historyRepository: HistoryRepository,
    private val authRepository: AuthRepository
) : BaseRowsViewModel() {

    companion object {
        private val linkPattern = Regex("\\/release\\/([^.]+?)\\.html")

        const val RELEASE_ROW_ID = 1L
        const val RELATED_ROW_ID = 2L
        const val RECOMMENDS_ROW_ID = 3L

        fun getReleasesFromDesc(description: String): List<String> {
            return linkPattern.findAll(description).map { it.groupValues[1] }.toList()
        }
    }

    var releaseId: Int = -1

    override val rowIds: List<Long> = listOf(RELEASE_ROW_ID, RELATED_ROW_ID, RECOMMENDS_ROW_ID)

    override val availableRows: MutableSet<Long> = mutableSetOf(RELEASE_ROW_ID, RELATED_ROW_ID, RECOMMENDS_ROW_ID)

    override fun onCreate() {
        super.onCreate()

        loadRelease()

        authRepository
            .observeUser()
            .map { it.authState }
            .distinctUntilChanged()
            .skip(1)
            .lifeSubscribe {
                loadRelease()
            }

        (releaseInteractor.getFull(releaseId) ?: releaseInteractor.getItem(releaseId))?.also {
            val releases = getReleasesFromDesc(it.description.orEmpty())
            updateAvailableRow(RELATED_ROW_ID, releases.isNotEmpty())
        }

        releaseInteractor
            .observeFull(releaseId)
            .map { getReleasesFromDesc(it.description.orEmpty()) }
            .lifeSubscribe {
                updateAvailableRow(RELATED_ROW_ID, it.isNotEmpty())
            }
    }

    private fun loadRelease() {
        releaseInteractor
            .loadRelease(releaseId)
            .map { historyRepository.putRelease(it) }
            .lifeSubscribe { }
    }
}