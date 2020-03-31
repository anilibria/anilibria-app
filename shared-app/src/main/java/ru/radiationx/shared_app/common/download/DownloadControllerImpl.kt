package ru.radiationx.shared_app.common.download

import android.app.DownloadManager
import android.content.*
import android.net.Uri
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.Maybe
import io.reactivex.Observable
import ru.radiationx.shared_app.common.MimeTypeUtil
import ru.radiationx.shared_app.common.SystemUtils
import toothpick.InjectConstructor

@InjectConstructor
class DownloadControllerImpl(
    private val context: Context,
    private val dataSource: DownloadsDataSource,
    private val systemUtils: SystemUtils
) : DownloadController, LifecycleObserver {

    private val downloadManager by lazy { context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager }

    init {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun start() {
        Log.e("DownloadController", "start")
        dataSource.enableObserving(true)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stop() {
        Log.e("DownloadController", "stop")
        dataSource.enableObserving(false)
    }

    override fun startDownload(url: String) {
        Log.e("DownloadController", "startDownload $url")
        val downloadId = enqueueDownload(url, null)
        dataSource.notifyDownloadStart(downloadId)
    }

    override fun removeDownload(url: String) {
        Log.e("DownloadController", "removeDownload $url")
        getDownload(url)?.also {
            downloadManager.remove(it.downloadId)
            dataSource.notifyDownloadRemove(it.downloadId)
        }
    }

    override fun getDownload(url: String): DownloadItem? = dataSource.getDownloads().firstOrNull { it.url == url }

    override fun observeDownload(url: String): Observable<DownloadItem> = dataSource
        .observeDownload()
        .filter { it.url == url }

    override fun observeCompleted(url: String): Observable<DownloadItem> = dataSource
        .observeCompleted()
        .filter { it.url == url }

    private fun enqueueDownload(url: String, name: String?): Long {
        val fileName = name ?: systemUtils.getFileNameFromUrl(url)
        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            setMimeType(MimeTypeUtil.getType(fileName))
            setTitle(fileName)
            setDescription(fileName)
        }
        return downloadManager.enqueue(request)
    }
}