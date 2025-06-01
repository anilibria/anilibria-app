package ru.radiationx.anilibria.screen.details

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.common.BaseRowsViewModel
import ru.radiationx.data.api.auth.AuthRepository
import ru.radiationx.data.api.franchises.FranchisesRepository
import ru.radiationx.data.api.franchises.models.getAllReleases
import ru.radiationx.data.api.releases.ReleaseInteractor
import ru.radiationx.data.app.history.HistoryRepository
import ru.radiationx.shared.ktx.coRunCatching
import timber.log.Timber
import javax.inject.Inject

class DetailsViewModel @Inject constructor(
    argExtra: DetailExtra,
    private val releaseInteractor: ReleaseInteractor,
    private val historyRepository: HistoryRepository,
    private val franchisesRepository: FranchisesRepository,
    authRepository: AuthRepository,
) : BaseRowsViewModel() {

    companion object {
        const val RELEASE_ROW_ID = 1L
        const val RELATED_ROW_ID = 2L
        const val RECOMMENDS_ROW_ID = 3L
    }

    private val releaseId = argExtra.id

    override val rowIds: List<Long> = listOf(RELEASE_ROW_ID, RELATED_ROW_ID, RECOMMENDS_ROW_ID)

    override val availableRows: MutableSet<Long> =
        mutableSetOf(RELEASE_ROW_ID, RELATED_ROW_ID, RECOMMENDS_ROW_ID)

    init {
        loadRelease()

        authRepository
            .observeAuthState()
            .drop(1)
            .distinctUntilChanged()
            .onEach { loadRelease() }
            .launchIn(viewModelScope)

        releaseInteractor
            .observeFull(releaseId)
            .onStart {
                releaseInteractor.getItem(releaseId)?.also {
                    emit(it)
                }
            }
            .map { release ->
                franchisesRepository
                    .getReleaseFranchises(releaseId)
                    .getAllReleases()
            }
            .distinctUntilChanged()
            .onEach {
                updateAvailableRow(RELATED_ROW_ID, it.isNotEmpty())
            }
            .launchIn(viewModelScope)
    }

    private fun loadRelease() {
        viewModelScope.launch {
            coRunCatching {
                releaseInteractor.loadRelease(releaseId)
            }.onSuccess {
                historyRepository.putRelease(it)
            }.onFailure {
                Timber.e(it)
            }
        }
    }
}