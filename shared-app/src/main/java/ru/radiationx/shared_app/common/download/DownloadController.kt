package ru.radiationx.shared_app.common.download

import io.reactivex.Observable

interface DownloadController {

    fun getDownload(url: String): DownloadItem?

    fun enqueueDownload(url: String, name: String? = null): DownloadItem
    fun observeState(url: String): Observable<State>
    fun observeProgress(url: String): Observable<Int>
    fun cancelDownload(url: String)

    enum class State {
        PENDING,
        RUNNING,
        PAUSED,
        SUCCESSFUL,
        FAILED
    }
}