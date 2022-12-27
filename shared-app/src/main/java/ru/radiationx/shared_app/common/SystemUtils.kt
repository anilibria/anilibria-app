package ru.radiationx.shared_app.common

import android.app.DownloadManager
import android.content.*
import android.net.Uri
import android.os.Environment
import android.util.Log
import timber.log.Timber
import toothpick.InjectConstructor
import java.io.UnsupportedEncodingException
import java.net.URLDecoder

@InjectConstructor
class SystemUtils(
    private val context: Context
) {

    /* PLEASE CHECK STORAGE PERMISSION */
    fun systemDownloader(url: String, fileName: String = getFileNameFromUrl(url)) {
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
        dm?.let {
            val request = DownloadManager.Request(Uri.parse(url))
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            request.setMimeType(MimeTypeUtil.getType(fileName))
            request.setTitle(fileName)
            request.setDescription(fileName)
            it.enqueue(request)
        }
    }

    fun getFileNameFromUrl(url: String): String {
        var fileName = url
        try {
            fileName = URLDecoder.decode(url, "CP1251")
        } catch (e: UnsupportedEncodingException) {
            Timber.e(e)
        }

        val cut = fileName.lastIndexOf('/')
        if (cut != -1) {
            fileName = fileName.substring(cut + 1)
        }
        return fileName
    }

    fun copyToClipBoard(s: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", s)
        clipboard.setPrimaryClip(clip)
    }

    fun readFromClipboard(): String? {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (clipboard.hasPrimaryClip()) {
            val description = clipboard.primaryClipDescription
            val data = clipboard.primaryClip
            if (data != null && description != null && description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN))
                return data.getItemAt(0).text.toString()
        }
        return null
    }

    fun shareText(text: String) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, text)
        sendIntent.type = "text/plain"
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(
            Intent.createChooser(sendIntent, "Поделиться").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    fun externalLink(url: String) {
        val intent =
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(
            Intent.createChooser(intent, "Открыть в").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    fun longLog(msg: String) {
        val maxLogSize = 1000
        for (i in 0..msg.length / maxLogSize) {
            val start = i * maxLogSize
            var end = (i + 1) * maxLogSize
            end = if (end > msg.length) msg.length else end
            Log.v("LONG_LOG", msg.substring(start, end))
        }
    }
}