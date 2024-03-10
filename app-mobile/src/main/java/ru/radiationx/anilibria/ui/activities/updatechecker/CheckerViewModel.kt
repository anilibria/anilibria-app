package ru.radiationx.anilibria.ui.activities.updatechecker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.data.analytics.features.UpdaterAnalytics
import ru.radiationx.data.downloader.DownloadedFile
import ru.radiationx.data.downloader.LocalFile
import ru.radiationx.data.downloader.RemoteFileRepository
import ru.radiationx.data.downloader.RemoteFile
import ru.radiationx.data.downloader.toLocalFile
import ru.radiationx.data.entity.domain.updater.UpdateData
import ru.radiationx.data.repository.CheckerRepository
import ru.radiationx.quill.QuillExtra
import ru.radiationx.shared.ktx.EventFlow
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
    private val systemUtils: SystemUtils,
    private val remoteFileRepository: RemoteFileRepository,
) : ViewModel() {

    private val loadingJobs = mutableMapOf<UpdateData.UpdateLink, Job>()

    private val _currentLoadings =
        MutableStateFlow<Map<UpdateData.UpdateLink, MutableStateFlow<Int>>>(emptyMap())
    private val _currentData = MutableStateFlow(CheckerScreenState())

    val openDownloadedFileAction = EventFlow<LocalFile>()

    val state = combine(
        _currentLoadings,
        _currentData
    ) { loadings, data ->
        if (data.data == null) return@combine data
        val newLinks = data.data.links.map {
            it.copy(progress = loadings[it.link])
        }
        data.copy(data = data.data.copy(links = newLinks))
    }

    fun submitUseTime(time: Long) {
        updaterAnalytics.useTime(time)
    }

    fun checkUpdate(force: Boolean = false) {
        viewModelScope.launch {
            _currentData.update { it.copy(loading = true) }
            coRunCatching {
                checkerRepository.checkUpdate(force || argExtra.forceLoad)
            }.onSuccess { data ->
                _currentData.update { it.copy(data = data.toState()) }
            }.onFailure {
                errorHandler.handle(it)
            }
            _currentData.update { it.copy(loading = false) }
        }
    }

    fun onLinkClick(link: UpdateData.UpdateLink) {
        updaterAnalytics.downloadClick()
        updaterAnalytics.sourceDownload(link.name)
        when (link.type) {
            UpdateData.LinkType.FILE -> downloadFile(link)
            UpdateData.LinkType.SITE -> systemUtils.externalLink(link.url)
        }
    }

    fun onCancelDownloadClick(link: UpdateData.UpdateLink) {
        loadingJobs[link]?.cancel()
        loadingJobs.remove(link)
        _currentLoadings.update { it.minus(link) }
    }

    private fun downloadFile(link: UpdateData.UpdateLink) {
        if (loadingJobs[link]?.isActive == true) {
            return
        }
        loadingJobs[link] = viewModelScope.launch {
            val progress = MutableStateFlow(0)
            _currentLoadings.update {
                it.plus(link to progress)
            }
            coRunCatching {
                remoteFileRepository.loadFile(link.url, RemoteFile.Bucket.AppUpdates, progress)
            }.onSuccess {
                openDownloadedFileAction.set(it.toLocalFile())
            }.onFailure {
                errorHandler.handle(it)
            }
            _currentLoadings.update {
                it.minus(link)
            }
        }
    }
}

private fun UpdateData.UpdateLink.toState() = UpdateLinkState(
    link = this,
    progress = null
)

private fun UpdateData.toState() = UpdateDataState(
    hasUpdate = hasUpdate,
    code = code,
    name = name,
    date = date,
    links = links.map { it.toState() },
    info = listOf(
        UpdateInfoState("Важно", important),
        UpdateInfoState("Добавлено", added),
        UpdateInfoState("Исправлено", fixed),
        UpdateInfoState("Изменено", changed),
    )
)

data class CheckerScreenState(
    val loading: Boolean = false,
    val data: UpdateDataState? = null,
)

data class UpdateDataState(
    val hasUpdate: Boolean,
    val code: Int,
    val name: String?,
    val date: String?,
    val links: List<UpdateLinkState>,
    val info: List<UpdateInfoState>,
)

data class UpdateInfoState(
    val title: String,
    val items: List<String>,
)

data class UpdateLinkState(
    val link: UpdateData.UpdateLink,
    val progress: MutableStateFlow<Int>?,
)