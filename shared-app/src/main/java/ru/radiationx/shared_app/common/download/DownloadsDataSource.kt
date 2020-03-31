package ru.radiationx.shared_app.common.download

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.util.Log
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import toothpick.InjectConstructor
import java.util.concurrent.TimeUnit

@InjectConstructor
class DownloadsDataSource(
    private val context: Context
) {

    private val TAG = "DownloadsDataSource"

    private val downloadManager by lazy { context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager }

    private val completeFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
    private val completeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            Log.e(TAG, "onReceive $downloadId, ${fetchDownloadRow(downloadId)}, ${findCached(downloadId)}")
            updateComplete(downloadId)
        }
    }

    private val handler = Handler()
    private val observesMap = mutableMapOf<String, ContentObserver>()

    private val cachedDownloads = mutableListOf<DownloadItem>()

    private val downloadsRelay = PublishRelay.create<DownloadItem>()
    private val completeRelay = PublishRelay.create<DownloadItem>()

    private var pendingTimerDisposable = Disposables.disposed()
    private val pendingDownloads = mutableListOf<Long>()

    fun getDownloads(): List<DownloadItem> = cachedDownloads.toList()

    fun observeDownload(): Observable<DownloadItem> = downloadsRelay.hide()

    fun observeCompleted(): Observable<DownloadItem> = completeRelay.hide()

    fun notifyDownloadStart(downloadId: Long) {
        Log.e(TAG, "notifyDownloadStart $downloadId")
        pendingDownloads.add(downloadId)
        fetchPendingDownloads()
    }

    fun notifyDownloadRemove(downloadId: Long) {
        Log.e(TAG, "notifyDownloadRemove $downloadId")
        stopObserver(downloadId)
    }

    fun enableObserving(enabled: Boolean) {
        Log.e(TAG, "enableObserving $enabled")
        if (enabled) {
            startTimer()
            context.registerReceiver(completeReceiver, completeFilter)
            cachedDownloads.forEach { startObserve(it.downloadId) }
            updateAll()
        } else {
            stopTimer()
            context.unregisterReceiver(completeReceiver)
            cachedDownloads.forEach { stopObserver(it.downloadId) }
        }
    }

    private fun startTimer() {
        stopTimer()
        pendingTimerDisposable = Observable
            .interval(1L, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                fetchPendingDownloads()
            }
    }

    private fun stopTimer() {
        pendingTimerDisposable.dispose()
    }

    private fun fetchPendingDownloads() {
        if (pendingDownloads.isEmpty()) {
            return
        }
        val downloadIds = pendingDownloads.toLongArray()
        val downloads = fetchDownloadRows(downloadIds)
        Log.e(TAG, "fetchPendingDownloads ids=${downloadIds.size}, fetched=${downloads.size}")
        downloads.forEach {
            updateCache(it)
            startObserve(it.downloadId)
            downloadsRelay.accept(it)
            pendingDownloads.remove(it.downloadId)
        }
    }

    private fun fetchDownloadRows(downloadIds: LongArray): List<DownloadItem> {
        val query = DownloadManager.Query().setFilterById(*downloadIds)
        val downloadItems = mutableListOf<DownloadItem>()
        downloadManager.query(query)?.use {
            if (it.moveToFirst()) {
                while (!it.isAfterLast) {
                    downloadItems.add(it.asDownloadItem())
                    it.moveToNext()
                }
            }
        }
        return downloadItems
    }

    private fun fetchDownloadRow(downloadId: Long): DownloadItem? {
        return fetchDownloadRows(longArrayOf(downloadId)).firstOrNull()
    }

    private fun updateAll() {
        /*if (cachedDownloads.isEmpty()) {
            return
        }*/
        val downloadIds = cachedDownloads.map { it.downloadId }.toLongArray()
        Log.e(TAG, "updateAll ${downloadIds.joinToString()}")
        val downloads = fetchDownloadRows(downloadIds)
        fullUpdateCache(downloads)
        downloads.forEach {
            Log.e(TAG, "updateAll new $it")
            startObserve(it.downloadId)
            downloadsRelay.accept(it)
        }
    }

    private fun update(downloadId: Long) {
        Log.e(TAG, "update $downloadId")
        fetchDownloadRow(downloadId)?.also {
            updateCache(it)
            downloadsRelay.accept(it)
        }
    }

    private fun updateComplete(downloadId: Long) {
        Log.e(TAG, "updateComplete $downloadId")
        findCached(downloadId)?.also {
            if (it.state != DownloadController.State.SUCCESSFUL) {
                cachedDownloads.removeAll { it.downloadId == downloadId }
            }
            completeRelay.accept(it)
        }
    }

    private fun startObserve(downloadId: Long) {
        Log.e(TAG, "startObserve $downloadId")
        val localUrl = findCached(downloadId)?.localUrl ?: return
        val contentObserver = observesMap[localUrl] ?: createContentObserver()
        Log.e(TAG, "startObserve localUrl $localUrl")
        context.contentResolver.registerContentObserver(Uri.parse(localUrl), false, contentObserver)
        observesMap[localUrl] = contentObserver
    }

    private fun stopObserver(downloadId: Long) {
        Log.e(TAG, "stopObserver $downloadId")
        val localUrl = findCached(downloadId)?.localUrl ?: return
        val contentObserver = observesMap[localUrl] ?: return
        Log.e(TAG, "stopObserver localUrl $localUrl")
        context.contentResolver.unregisterContentObserver(contentObserver)
    }

    private fun fullUpdateCache(items: List<DownloadItem>) {
        cachedDownloads.clear()
        cachedDownloads.addAll(items)
        Log.e(TAG, "fullUpdateCache complete ${cachedDownloads.map { it.downloadId }}")
    }

    private fun updateCache(downloadItem: DownloadItem) {
        cachedDownloads.removeAll { it.downloadId == downloadItem.downloadId }
        cachedDownloads.add(downloadItem)
        Log.e(TAG, "updateCache complete ${cachedDownloads.map { it.downloadId }}")
    }

    private fun findCached(downloadId: Long): DownloadItem? = cachedDownloads.firstOrNull { it.downloadId == downloadId }

    private fun findCached(localUrl: String): DownloadItem? = cachedDownloads.firstOrNull { it.localUrl == localUrl }

    private fun createContentObserver() = object : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            uri ?: return
            Log.e(TAG, "onChange $uri")
            findCached(uri.toString())?.downloadId?.also { update(it) }
        }
    }

    private fun Cursor.asDownloadItem(): DownloadItem {
        val id = getLong(getColumnIndex(DownloadManager.COLUMN_ID))
        val url = getString(getColumnIndex(DownloadManager.COLUMN_URI))
        val localUrl = getString(getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
        val status = getInt(getColumnIndex(DownloadManager.COLUMN_STATUS))
        val reason = getInt(getColumnIndex(DownloadManager.COLUMN_REASON))
        val allBytes = getLong(getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
        val loadedBytes = getLong(getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))

        val progress = let {
            if (allBytes <= 0L || loadedBytes <= 0L) {
                return@let 0
            }
            ((loadedBytes / allBytes.toDouble()) * 100).toInt()
        }

        return DownloadItem(id, url, localUrl, progress, status.asState(), reason.asReason())
    }

    private fun Int.asState() = when (this) {
        DownloadManager.STATUS_FAILED -> DownloadController.State.FAILED
        DownloadManager.STATUS_PAUSED -> DownloadController.State.PAUSED
        DownloadManager.STATUS_PENDING -> DownloadController.State.PENDING
        DownloadManager.STATUS_RUNNING -> DownloadController.State.RUNNING
        DownloadManager.STATUS_SUCCESSFUL -> DownloadController.State.SUCCESSFUL
        else -> throw IllegalStateException("Unknown state $this")
    }

    private fun Int.asReason() = when (this) {
        DownloadManager.ERROR_UNKNOWN -> DownloadController.Reason.ERROR_UNKNOWN
        DownloadManager.ERROR_FILE_ERROR -> DownloadController.Reason.ERROR_FILE_ERROR
        DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> DownloadController.Reason.ERROR_UNHANDLED_HTTP_CODE
        DownloadManager.ERROR_HTTP_DATA_ERROR -> DownloadController.Reason.ERROR_HTTP_DATA_ERROR
        DownloadManager.ERROR_TOO_MANY_REDIRECTS -> DownloadController.Reason.ERROR_TOO_MANY_REDIRECTS
        DownloadManager.ERROR_INSUFFICIENT_SPACE -> DownloadController.Reason.ERROR_INSUFFICIENT_SPACE
        DownloadManager.ERROR_DEVICE_NOT_FOUND -> DownloadController.Reason.ERROR_DEVICE_NOT_FOUND
        DownloadManager.ERROR_CANNOT_RESUME -> DownloadController.Reason.ERROR_CANNOT_RESUME
        DownloadManager.ERROR_FILE_ALREADY_EXISTS -> DownloadController.Reason.ERROR_FILE_ALREADY_EXISTS
        DownloadManager.PAUSED_WAITING_TO_RETRY -> DownloadController.Reason.PAUSED_WAITING_TO_RETRY
        DownloadManager.PAUSED_WAITING_FOR_NETWORK -> DownloadController.Reason.PAUSED_WAITING_FOR_NETWORK
        DownloadManager.PAUSED_QUEUED_FOR_WIFI -> DownloadController.Reason.PAUSED_QUEUED_FOR_WIFI
        DownloadManager.PAUSED_UNKNOWN -> DownloadController.Reason.PAUSED_UNKNOWN
        else -> null
    }

}