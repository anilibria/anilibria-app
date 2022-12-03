package ru.radiationx.anilibria.ui.activities.updatechecker

import android.Manifest
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.mintrocket.lib.mintpermissions.flows.MintPermissionsDialogFlow
import ru.mintrocket.lib.mintpermissions.flows.ext.isSuccess
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.analytics.features.UpdaterAnalytics
import ru.radiationx.data.entity.domain.updater.UpdateData
import ru.radiationx.data.repository.CheckerRepository
import ru.radiationx.quill.QuillExtra
import ru.radiationx.shared_app.common.SystemUtils
import toothpick.InjectConstructor

data class CheckerExtra(
    val forceLoad: Boolean
) : QuillExtra

@InjectConstructor
class CheckerViewModel(
    private val argExtra: CheckerExtra,
    private val checkerRepository: CheckerRepository,
    private val errorHandler: IErrorHandler,
    private val updaterAnalytics: UpdaterAnalytics,
    private val sharedBuildConfig: SharedBuildConfig,
    private val systemUtils: SystemUtils,
    private val mintPermissionsDialogFlow: MintPermissionsDialogFlow
) : ViewModel() {

    private val _state = MutableStateFlow(CheckerScreenState())
    val state = _state.asStateFlow()

    fun submitUseTime(time: Long) {
        updaterAnalytics.useTime(time)
    }

    fun checkUpdate() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            runCatching {
                checkerRepository.checkUpdate(sharedBuildConfig.versionCode, argExtra.forceLoad)
            }.onSuccess { data ->
                _state.update { it.copy(data = data) }
            }.onFailure {
                errorHandler.handle(it)
            }
            _state.update { it.copy(loading = false) }
        }
    }

    fun onDownloadClick() {
        updaterAnalytics.downloadClick()
    }

    fun onSourceDownloadClick(link: UpdateData.UpdateLink) {
        updaterAnalytics.sourceDownload(link.name)
        decideDownload(link)
    }

    private fun decideDownload(link: UpdateData.UpdateLink) {
        when (link.type) {
            "file" -> downloadFile(link)
            "site" -> systemUtils.externalLink(link.url)
            else -> systemUtils.externalLink(link.url)
        }
    }

    private fun downloadFile(link: UpdateData.UpdateLink) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            systemUtils.systemDownloader(link.url)
            return
        }
        viewModelScope.launch {
            val result =
                mintPermissionsDialogFlow.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (result.isSuccess()) {
                systemUtils.systemDownloader(link.url)
            }
        }
    }
}

data class CheckerScreenState(
    val loading: Boolean = false,
    val data: UpdateData? = null
)