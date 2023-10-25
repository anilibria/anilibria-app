package ru.radiationx.anilibria.screen.update

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.anilibria.screen.UpdateSourceScreen
import ru.radiationx.data.downloader.DownloadState
import ru.radiationx.data.downloader.FileDownloaderRepository
import ru.radiationx.data.downloader.RemoteFile
import ru.radiationx.data.entity.domain.updater.UpdateData
import ru.radiationx.data.repository.CheckerRepository
import ru.radiationx.shared.ktx.coRunCatching
import ru.radiationx.shared_app.common.SystemUtils
import timber.log.Timber
import toothpick.InjectConstructor

@InjectConstructor
class UpdateViewModel(
    private val checkerRepository: CheckerRepository,
    private val guidedRouter: GuidedRouter,
    private val updateController: UpdateController,
    private val fileDownloaderRepository: FileDownloaderRepository,
    private val systemUtils: SystemUtils,
) : LifecycleViewModel() {

    val updateData = MutableStateFlow<UpdateData?>(null)
    val progressState = MutableStateFlow(false)
    val downloadProgressShowState = MutableStateFlow(false)
    val downloadProgressData = MutableStateFlow(0)
    val downloadActionTitle = MutableStateFlow<String?>(null)

    init {
        progressState.value = true
        downloadProgressShowState.value = false
        downloadProgressData.value = 0

        viewModelScope.launch {
            progressState.value = true
            coRunCatching {
                checkerRepository.checkUpdate(false)
            }.onSuccess { update ->
                updateData.value = update
            }.onFailure {
                Timber.e(it)
            }
            progressState.value = false
        }
        updateController
            .downloadAction
            .onEach {
                startDownload(it.url)
            }
            .launchIn(viewModelScope)
    }

    fun onActionClick() {
        TODO()
    }

    private fun downloadClick() {
        val data = updateData.value ?: return
        if (data.links.size > 1) {
            guidedRouter.open(UpdateSourceScreen())
        } else {
            val link = data.links.firstOrNull() ?: return
            updateController.downloadAction.emit(link)
        }
    }

    private fun startDownload(url: String) {
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