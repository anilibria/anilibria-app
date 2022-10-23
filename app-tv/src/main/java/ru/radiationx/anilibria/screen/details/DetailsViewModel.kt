package ru.radiationx.anilibria.screen.details

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.common.BaseRowsViewModel
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.data.repository.HistoryRepository
import timber.log.Timber
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

    override val availableRows: MutableSet<Long> =
        mutableSetOf(RELEASE_ROW_ID, RELATED_ROW_ID, RECOMMENDS_ROW_ID)

    override fun onCreate() {
        super.onCreate()

        loadRelease()

        authRepository
            .observeUser()
            .map { it.authState }
            .distinctUntilChanged()
            .drop(1)
            .onEach {
                loadRelease()
            }
            .launchIn(viewModelScope)

        (releaseInteractor.getFull(releaseId) ?: releaseInteractor.getItem(releaseId))?.also {
            val releases = getReleasesFromDesc(it.description.orEmpty())
            updateAvailableRow(RELATED_ROW_ID, releases.isNotEmpty())
        }

        releaseInteractor
            .observeFull(releaseId)
            .map { getReleasesFromDesc(it.description.orEmpty()) }
            .onEach {
                updateAvailableRow(RELATED_ROW_ID, it.isNotEmpty())
            }
            .launchIn(viewModelScope)
    }

    private fun loadRelease() {
        viewModelScope.launch{
            runCatching {
                releaseInteractor.loadRelease(releaseId)
            }.onSuccess {
                historyRepository.putRelease(it)
            }.onFailure {
                Timber.e(it)
            }
        }
    }
}