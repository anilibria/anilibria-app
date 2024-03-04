package ru.radiationx.anilibria.ui.fragments.history

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.ui.common.ErrorHandler
import ru.radiationx.anilibria.utils.messages.SystemMessenger
import ru.radiationx.data.historyfile.HistoryFileRepository
import ru.radiationx.shared.ktx.coRunCatching
import ru.radiationx.shared_app.common.SystemUtils
import toothpick.InjectConstructor

@InjectConstructor
class HistoryFileViewModel(
    private val historyFileRepository: HistoryFileRepository,
    private val systemUtils: SystemUtils,
    private val errorHandler: ErrorHandler,
    private val systemMessenger: SystemMessenger,
) : ViewModel() {

    fun onExportClick() {
        viewModelScope.launch {
            coRunCatching {
                historyFileRepository.exportFile()
            }.onSuccess {
                systemUtils.shareLocalFile(it)
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    fun onImportFileSelected(uri: Uri) {
        viewModelScope.launch {
            coRunCatching {
                historyFileRepository.importFile(uri)
            }.onSuccess {
                systemMessenger.showMessage("История успешно импортирована")
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }
}