package ru.radiationx.anilibria.screen.details

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.common.BaseRowsViewModel
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.data.repository.HistoryRepository
import ru.radiationx.shared.ktx.coRunCatching
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel, управляющий «списком строк» (ID=1,2,3), которые показываются в DetailFragment:
 *  1) RELEASE_ROW_ID   — «шапка» (DetailHeaderViewModel)
 *  2) RELATED_ROW_ID   — «связанные» релизы
 *  3) RECOMMENDS_ROW_ID — «рекомендации»
 */
class DetailsViewModel @Inject constructor(
    argExtra: DetailExtra,
    private val releaseInteractor: ReleaseInteractor,
    private val historyRepository: HistoryRepository,
    authRepository: AuthRepository,
) : BaseRowsViewModel() {

    companion object {
        const val RELEASE_ROW_ID = 1L
        const val RELATED_ROW_ID = 2L
        const val RECOMMENDS_ROW_ID = 3L
    }

    // Список потенциальных rowId
    override val rowIds: List<Long> = listOf(
        RELEASE_ROW_ID,
        RELATED_ROW_ID,
        RECOMMENDS_ROW_ID
    )

    // Доступные (актуальные) строки. В начале все включены.
    override val availableRows: MutableSet<Long> = mutableSetOf(
        RELEASE_ROW_ID, RELATED_ROW_ID, RECOMMENDS_ROW_ID
    )

    private val releaseId = argExtra.id

    init {
        // Загрузим релиз (чтобы, например, не было пустых данных)
        loadRelease()

        // Если статус авторизации меняется → тоже перегрузим
        authRepository
            .observeAuthState()
            .drop(1)
            .distinctUntilChanged()
            .onEach {
                loadRelease()
            }
            .launchIn(viewModelScope)

        // Если у релиза появятся франшизы (или наоборот) → обновим строку RELATED
        releaseInteractor
            .observeFull(releaseId)
            .onEach { release ->
                val hasFranchises = release.getFranchisesIds().any { it != release.id }
                updateAvailableRow(RELATED_ROW_ID, hasFranchises)
            }
            .launchIn(viewModelScope)
    }

    /**
     * Метод для загрузки релиза из сети/кэша.
     * Затем помещаем его в «историю» (HistoryRepository).
     */
    private fun loadRelease() {
        viewModelScope.launch {
            coRunCatching {
                releaseInteractor.loadRelease(releaseId)
            }.onSuccess { release ->
                // Положим в history (чтобы его учитывали в рекомендациях и т.д.)
                historyRepository.putRelease(release)
            }.onFailure { error ->
                Timber.e(error)
            }
        }
    }
}
