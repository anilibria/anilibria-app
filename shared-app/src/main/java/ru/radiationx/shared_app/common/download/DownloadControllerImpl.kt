package ru.radiationx.shared_app.common.download

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import ru.radiationx.shared_app.common.MimeTypeUtil
import ru.radiationx.shared_app.common.SystemUtils
import toothpick.InjectConstructor

@InjectConstructor
class DownloadControllerImpl(
    private val context: Context,
    private val dataSource: DownloadsDataSource,
    private val systemUtils: SystemUtils,
) : DownloadController, DefaultLifecycleObserver {

    private val downloadManager by lazy { context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        dataSource.enableObserving(true)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        dataSource.enableObserving(false)
    }

    override fun startDownload(url: String) {
        val downloadId = enqueueDownload(url, null)
        dataSource.notifyDownloadStart(downloadId)
    }

    override fun removeDownload(url: String) {
        getDownload(url)?.also {
            downloadManager.remove(it.downloadId)
            dataSource.notifyDownloadRemove(it.downloadId)
        }
    }

    override fun getDownload(url: String): DownloadItem? =
        dataSource.getDownloads().firstOrNull { it.url == url }

    override fun observeDownload(url: String): Flow<DownloadItem> = dataSource
        .observeDownload()
        .filter { it.url == url }

    override fun observeCompleted(url: String): Flow<DownloadItem> = dataSource
        .observeCompleted()
        .filter { it.url == url }

    private fun enqueueDownload(url: String, name: String?): Long {
        val fileName = name ?: systemUtils.getFileNameFromUrl(url)
        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            }
            setMimeType(MimeTypeUtil.getType(fileName))
            setTitle(fileName)
            setDescription(fileName)
        }
        return downloadManager.enqueue(request)
    }
}