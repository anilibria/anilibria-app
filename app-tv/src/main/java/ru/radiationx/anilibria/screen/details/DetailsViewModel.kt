package ru.radiationx.anilibria.screen.details

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.common.BaseRowsViewModel
import ru.radiationx.data.entity.domain.types.ReleaseCode
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.data.repository.HistoryRepository
import ru.radiationx.shared.ktx.coRunCatching
import timber.log.Timber
import toothpick.InjectConstructor

@InjectConstructor
class DetailsViewModel(
    private val argExtra: DetailExtra,
    private val releaseInteractor: ReleaseInteractor,
    private val historyRepository: HistoryRepository,
    private val authRepository: AuthRepository
) : BaseRowsViewModel() {

    companion object {
        private val linkPattern = Regex("\\/release\\/([^.]+?)\\.html")

        const val RELEASE_ROW_ID = 1L
        const val RELATED_ROW_ID = 2L
        const val RECOMMENDS_ROW_ID = 3L

        fun getReleasesFromDesc(description: String): List<ReleaseCode> {
            return linkPattern
                .findAll(description)
                .map { it.groupValues[1] }
                .map { ReleaseCode(it) }
                .toList()
        }
    }

    private val releaseId = argExtra.id

    override val rowIds: List<Long> = listOf(RELEASE_ROW_ID, RELATED_ROW_ID, RECOMMENDS_ROW_ID)

    override val availableRows: MutableSet<Long> =
        mutableSetOf(RELEASE_ROW_ID, RELATED_ROW_ID, RECOMMENDS_ROW_ID)

    override fun onCreate() {
        super.onCreate()
        loadRelease()
    }

    override fun onColdResume() {
        super.onColdResume()
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
            .map { it.description.orEmpty() }
            .distinctUntilChanged()
            .map { getReleasesFromDesc(it) }
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