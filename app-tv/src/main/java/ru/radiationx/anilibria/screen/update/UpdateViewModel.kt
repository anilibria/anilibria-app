package ru.radiationx.anilibria.screen.update

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.anilibria.screen.UpdateSourceScreen
import ru.radiationx.data.downloader.RemoteFile
import ru.radiationx.data.downloader.RemoteFileRepository
import ru.radiationx.data.downloader.toLocalFile
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
    private val remoteFileRepository: RemoteFileRepository,
    private val systemUtils: SystemUtils,
) : LifecycleViewModel() {

    private var downloadJob: Job? = null

    val updateData = MutableStateFlow<UpdateData?>(null)
    val progressState = MutableStateFlow(false)
    val downloadProgressShowState = MutableStateFlow(false)
    val downloadProgressData = MutableStateFlow(0)

    init {
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
        if (downloadProgressShowState.value) {
            cancelDownloadClick()
        } else {
            downloadClick()
        }
    }

    private fun downloadClick() {
        val data = updateData.value ?: return
        if (data.links.size > 1) {
            guidedRouter.open(UpdateSourceScreen())
        } else {
            val link = data.links.firstOrNull() ?: return
            startDownload(link.url)
        }
    }

    private fun cancelDownloadClick() {
        downloadJob?.cancel()
        downloadJob = null
        downloadProgressShowState.value = false
    }

    private fun startDownload(url: String) {
        if (downloadJob?.isActive == true) {
            return
        }
        downloadJob = viewModelScope.launch {
            downloadProgressShowState.value = true
            coRunCatching {
                remoteFileRepository.loadFile(
                    url,
                    RemoteFile.Bucket.AppUpdates,
                    downloadProgressData
                )
            }.onSuccess {
                systemUtils.openLocalFile(it.toLocalFile())
            }.onFailure {
                Timber.e(it)
            }
            downloadProgressShowState.value = false
        }
    }

}