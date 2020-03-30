package ru.radiationx.shared_app.common.download

import android.app.DownloadManager
import android.content.*
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.shared.ktx.android.mapTo
import ru.radiationx.shared_app.common.MimeTypeUtil
import toothpick.InjectConstructor
import java.io.UnsupportedEncodingException
import java.lang.IllegalStateException
import java.net.URLDecoder

@InjectConstructor
class DownloadControllerImpl(
    private val context: Context,
    private val sharedPreferences: SharedPreferences
) : DownloadController, LifecycleObserver {

    private val downloadManager by lazy { context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager }

    private val completeFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
    private val completeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            Log.e("DownloadController", "onReceive $downloadId")
            updateById(downloadId)
        }
    }

    private val handler = Handler()
    private val observesMap = mutableMapOf<String, ContentObserver>()

    private val downloads = mutableListOf<DownloadItem>()

    private val stateMap = mutableMapOf<String, DownloadController.State>()
    private val progressMap = mutableMapOf<String, Int>()

    private val statesRelay = BehaviorRelay.create<Map<String, DownloadController.State>>()
    private val progressRelay = BehaviorRelay.create<Map<String, Int>>()

    init {
        Log.e("DownloadController", "init $context")
        //restore()
    }

    private fun restore() {
        val downloadsStr = sharedPreferences.getString("key_downloads", null) ?: return
        val downloadsJson = JSONArray(downloadsStr)
        downloads.clear()
        for (index in 0 until downloadsJson.length()) {
            val jsonItem = downloadsJson.getJSONObject(index)
            downloads.add(
                DownloadItem(
                    jsonItem.getLong("downloadId"),
                    jsonItem.getString("url"),
                    jsonItem.getString("localUrl")
                )
            )
        }
    }

    private fun save() {
        val downloadsJson = JSONArray()
        downloads.forEach {
            val jsonItem = JSONObject()
            jsonItem.put("downloadId", it.downloadId)
            jsonItem.put("url", it.url)
            jsonItem.put("localUrl", it.localUrl)
            downloadsJson.put(jsonItem)
        }
        sharedPreferences.edit().putString("key_downloads", downloadsJson.toString()).apply()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun start() {
        Log.e("DownloadController", "start")
        context.registerReceiver(completeReceiver, completeFilter)
        downloads.forEach { registerObserver(it.localUrl) }
        updateAll()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stop() {
        Log.e("DownloadController", "stop")
        context.unregisterReceiver(completeReceiver)
        downloads.forEach { unregisterObserver(it.localUrl) }
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
        val downloadRow = getDownloadInfo(downloadId)!!
        val localUrl = downloadRow.localUrl
        registerObserver(localUrl)
        val downloadItem = DownloadItem(downloadId, url, localUrl)
        downloads.add(downloadItem)
        save()
        return downloadItem
    }

    override fun observeState(url: String): Observable<DownloadController.State> = statesRelay
        .hide()
        .filter { it[url] != null }
        .map { it.getValue(url) }
        .distinctUntilChanged()
    /*.doOnSubscribe {
        val downloadItem = getDownload(url) ?: return@doOnSubscribe
        registerObserver(downloadItem.localUrl)
    }*/

    override fun observeProgress(url: String): Observable<Int> = progressRelay
        .hide()
        .filter { it[url] != null }
        .map { it.getValue(url) }
        .distinctUntilChanged()
    /*.doOnSubscribe {
        val downloadItem = getDownload(url) ?: return@doOnSubscribe
        registerObserver(downloadItem.localUrl)
    }*/

    override fun cancelDownload(url: String) {
        val downloadId = getDownload(url)?.downloadId ?: return
        downloadManager.remove(downloadId)
    }

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
        Log.e("DownloadController", "registerObserver $localUrl, ${observesMap.contains(localUrl)}")
        val contentObserver = observesMap[localUrl] ?: createContentObserver()
        try {
            context.contentResolver.registerContentObserver(Uri.parse(localUrl), false, contentObserver)
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }
        observesMap[localUrl] = contentObserver
    }

    private fun unregisterObserver(localUrl: String) {
        Log.e("DownloadController", "unregisterObserver $localUrl")
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

    private fun installIntent() {


    }
}