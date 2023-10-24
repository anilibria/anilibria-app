package ru.radiationx.anilibria.ui.activities.updatechecker

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.analytics.features.UpdaterAnalytics
import ru.radiationx.data.downloader.DownloadState
import ru.radiationx.data.downloader.FileDownloaderRepository
import ru.radiationx.data.downloader.RemoteFile
import ru.radiationx.data.entity.domain.updater.UpdateData
import ru.radiationx.data.repository.CheckerRepository
import ru.radiationx.quill.QuillExtra
import ru.radiationx.shared.ktx.coRunCatching
import ru.radiationx.shared_app.common.SystemUtils
import toothpick.InjectConstructor

data class CheckerExtra(
    val forceLoad: Boolean,
) : QuillExtra

@InjectConstructor
class CheckerViewModel(
    private val argExtra: CheckerExtra,
    private val checkerRepository: CheckerRepository,
    private val errorHandler: IErrorHandler,
    private val updaterAnalytics: UpdaterAnalytics,
    private val sharedBuildConfig: SharedBuildConfig,
    private val systemUtils: SystemUtils,
    private val fileDownloaderRepository: FileDownloaderRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CheckerScreenState())
    val state = _state.asStateFlow()

    fun submitUseTime(time: Long) {
        updaterAnalytics.useTime(time)
    }

    fun checkUpdate() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            coRunCatching {
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
        val url = link.url
        viewModelScope.launch {
            Log.d("kekeke", "testDownload luanch")
            fileDownloaderRepository.loadFile(url, RemoteFile.Bucket.AppUpdates).collect {
                Log.d("kekeke", "testDownload collect ${it}")
                if (it is DownloadState.Success) {
                    systemUtils.openRemoteFile(
                        it.file.local,
                        it.file.remote.name,
                        it.file.remote.mimeType
                    )
                }
            }
            Log.d("kekeke", "testDownload finish")
        }
    }
}

data class CheckerScreenState(
    val loading: Boolean = false,
    val data: UpdateData? = null,
)