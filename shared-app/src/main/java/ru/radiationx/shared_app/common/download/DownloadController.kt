package ru.radiationx.shared_app.common.download

import kotlinx.coroutines.flow.Flow

interface DownloadController {

    fun startDownload(url: String)
    fun removeDownload(url: String)

    fun getDownload(url: String): DownloadItem?

    fun observeDownload(url: String): Flow<DownloadItem>
    fun observeCompleted(url: String): Flow<DownloadItem>

    /*fun getDownload(url: String): DownloadItem?

    fun enqueueDownload(url: String, name: String? = null): DownloadItem
    fun observeState(url: String): Observable<State>
    fun observeProgress(url: String): Observable<Int>
    fun cancelDownload(url: String)*/

    enum class State {
        PENDING,
        RUNNING,
        PAUSED,
        SUCCESSFUL,
        FAILED
    }

    enum class Reason {
        ERROR_UNKNOWN,
        ERROR_FILE_ERROR,
        ERROR_UNHANDLED_HTTP_CODE,
        ERROR_HTTP_DATA_ERROR,
        ERROR_TOO_MANY_REDIRECTS,
        ERROR_INSUFFICIENT_SPACE,
        ERROR_DEVICE_NOT_FOUND,
        ERROR_CANNOT_RESUME,
        ERROR_FILE_ALREADY_EXISTS,

        PAUSED_WAITING_TO_RETRY,
        PAUSED_WAITING_FOR_NETWORK,
        PAUSED_QUEUED_FOR_WIFI,
        PAUSED_UNKNOWN
    }
}