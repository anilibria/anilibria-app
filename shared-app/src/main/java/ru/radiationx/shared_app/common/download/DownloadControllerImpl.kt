package ru.radiationx.shared_app.common.download

import android.app.DownloadManager
import android.content.*
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import ru.radiationx.shared_app.common.MimeTypeUtil
import toothpick.InjectConstructor
import java.io.UnsupportedEncodingException
import java.lang.IllegalStateException
import java.net.URLDecoder

@InjectConstructor
class DownloadControllerImpl(
    private val context: Context
) : DownloadController {

    private val downloadManager by lazy { context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager }

    private val completeFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
    private val completeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            updateById(downloadId)
        }
    }

    private val handler = Handler()
    private val observesMap = mutableMapOf<String, ContentObserver>()

    private val downloads = mutableListOf<DownloadItem>()
    private val info = mutableListOf<DownloadRow>()

    private val stateMap = mutableMapOf<String, DownloadController.State>()
    private val progressMap = mutableMapOf<String, Int>()

    private val statesRelay = BehaviorRelay.create<Map<String, DownloadController.State>>()
    private val progressRelay = BehaviorRelay.create<Map<String, Int>>()

    fun start() {
        context.registerReceiver(completeReceiver, completeFilter)
        info.forEach { registerObserver(it.localUrl) }
        updateAll()
    }

    fun stop() {
        context.unregisterReceiver(completeReceiver)
        info.forEach { unregisterObserver(it.localUrl) }
    }

    override fun getDownload(url: String): DownloadItem? = downloads.firstOrNull { it.url == url }

    override fun enqueueDownload(url: String, name: String?): DownloadItem {
        val fileName = name ?: getFileNameFromUrl(url)
        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            //setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            setMimeType(MimeTypeUtil.getType(fileName))
            setTitle(fileName)
            setDescription(fileName)
        }
        val downloadId = downloadManager.enqueue(request)
        val localUrl = getLocalUrl(downloadId)
        registerObserver(localUrl)
        val downloadItem = DownloadItem(downloadId, url, localUrl)
        downloads.add(downloadItem)
        return downloadItem
    }

    override fun observeState(url: String): Observable<DownloadController.State> = statesRelay
        .hide()
        .filter { it[url] != null }
        .map { it.getValue(url) }
        .distinctUntilChanged()

    override fun observeProgress(url: String): Observable<Int> = progressRelay
        .hide()
        .filter { it[url] != null }
        .map { it.getValue(url) }
        .distinctUntilChanged()

    private fun updateState(url: String, state: DownloadController.State) {
        stateMap[url] = state
        statesRelay.accept(stateMap)
    }

    private fun updateProgress(url: String, progress: Int) {
        progressMap[url] = progress
        progressRelay.accept(progressMap)
    }

    private fun updateAll() {
        val info = getAllDownloadsInfo()
        info.forEach {
            updateState(it.url, it.state)
            updateProgress(it.url, it.progress)
        }
    }

    private fun updateByUrl(localUrl: String) {
        val downloadId = downloads.firstOrNull { it.localUrl == localUrl }?.downloadId ?: return
        updateById(downloadId)
    }

    private fun updateById(downloadId: Long) {
        getDownloadInfo(downloadId)?.also {
            updateState(it.url, it.state)
            updateProgress(it.url, it.progress)
        }
    }

    private fun getAllDownloadsInfo(): List<DownloadRow> {
        val ids = downloads.map { it.downloadId }.toLongArray()
        val query = DownloadManager.Query().setFilterById(*ids)
        val info = mutableListOf<DownloadRow>()
        downloadManager.query(query)?.use {
            it.moveToFirst()
            while (it.moveToNext()) {
                info.add(it.toInfo())
            }
        }
        return info
    }

    private fun getDownloadInfo(downloadId: Long): DownloadRow? {
        val query = DownloadManager.Query().setFilterById(downloadId)
        return downloadManager.query(query)?.use {
            if (it.moveToFirst()) {
                it.toInfo()
            } else {
                null
            }
        }
    }

    private fun Cursor.toInfo(): DownloadRow {
        val id = getLong(getColumnIndex(DownloadManager.COLUMN_ID))
        val url = getString(getColumnIndex(DownloadManager.COLUMN_URI))
        val localUrl = getString(getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
        val status = getInt(getColumnIndex(DownloadManager.COLUMN_STATUS))
        val allBytes = getLong(getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
        val loadedBytes = getLong(getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))

        val state = status.asState()
        val progress = ((loadedBytes / allBytes.toDouble()) * 100).toInt()

        return DownloadRow(id, url, localUrl, state, progress)
    }

    private fun getLocalUrl(downloadId: Long): String {
        val query = DownloadManager.Query().setFilterById(downloadId)
        return downloadManager.query(query)
            ?.use {
                if (it.moveToFirst()) {
                    it.getString(it.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                } else {
                    null
                }
            }
            ?: throw IllegalStateException("No local url by $downloadId")
    }

    private fun registerObserver(localUrl: String) {
        val contentObserver = observesMap[localUrl] ?: createContentObserver()
        context.contentResolver.registerContentObserver(Uri.parse(localUrl), false, contentObserver)
    }

    private fun unregisterObserver(localUrl: String) {
        val contentObserver = observesMap[localUrl] ?: return
        context.contentResolver.unregisterContentObserver(contentObserver)
    }

    private data class DownloadRow(
        val downloadId: Long,
        val url: String,
        val localUrl: String,
        val state: DownloadController.State,
        val progress: Int
    )

    private fun createContentObserver() = object : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            uri ?: return
            updateByUrl(uri.toString())
        }
    }

    private fun Int.asState() = when (this) {
        DownloadManager.STATUS_FAILED -> DownloadController.State.FAILED
        DownloadManager.STATUS_PAUSED -> DownloadController.State.PAUSED
        DownloadManager.STATUS_PENDING -> DownloadController.State.PENDING
        DownloadManager.STATUS_RUNNING -> DownloadController.State.RUNNING
        DownloadManager.STATUS_SUCCESSFUL -> DownloadController.State.SUCCESSFUL
        else -> throw IllegalStateException("Unknown state $this")
    }

    private fun getFileNameFromUrl(url: String): String {
        var fileName = url
        try {
            fileName = URLDecoder.decode(url, "CP1251")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        val cut = fileName.lastIndexOf('/')
        if (cut != -1) {
            fileName = fileName.substring(cut + 1)
        }
        return fileName
    }
}