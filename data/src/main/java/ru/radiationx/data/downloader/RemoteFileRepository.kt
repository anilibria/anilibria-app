package ru.radiationx.data.downloader

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import ru.radiationx.data.SimpleClient
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.entity.common.Url
import timber.log.Timber
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

class RemoteFileRepository @Inject constructor(
    private val context: Context,
    @SimpleClient private val client: IClient,
    private val holder: RemoteFileHolder,
    private val apiConfig: ApiConfig
) {

    suspend fun loadFile(
        url: Url,
        bucket: RemoteFile.Bucket,
        progress: MutableStateFlow<Int>,
    ): DownloadedFile = withContext(Dispatchers.IO) {
        val absoluteUrl = url.absolute(apiConfig.apiUrl)
        progress.value = 0
        val existedFile = getDownloadedFile(absoluteUrl)
        if (existedFile != null) {
            return@withContext existedFile
        }

        val loadingFileId = holder.get(absoluteUrl)?.id ?: holder.generateId()
        val loadingFile = getFileById(loadingFileId)
        try {
            val response = client.getRaw(absoluteUrl, emptyMap())

            val responseBody = requireNotNull(response.body) {
                "Response doesn't contain a body"
            }
            // todo API2 await result
            /*require(responseBody.contentLength() > 0) {
                "Response content length <= 0 bytes"
            }*/
            if (responseBody.contentLength() < 0) {
                Timber.w("Response content length <= 0 bytes")
            }
            loadingFile.createNewFile()
            responseBody.copyToWithProgress(loadingFile).collect(progress)
            val saveData = RemoteFileSaveData(
                id = loadingFileId,
                url = absoluteUrl,
                bucket = bucket,
                contentDisposition = response.header("Content-Disposition"),
                contentType = response.header("Content-Type"),
            )
            val remoteFile = holder.put(saveData)
            DownloadedFile(remoteFile, loadingFile)
        } catch (ex: Exception) {
            loadingFile.delete()
            throw ex
        }
    }

    private fun getCacheDir(): File {
        val file = File(context.cacheDir, "anilibria_remote")
        file.mkdir()
        return file
    }

    private fun getFileById(id: RemoteFileId): File {
        val fileName = id.id.toString()
        return File(getCacheDir(), fileName)
    }

    private suspend fun getDownloadedFile(url: String): DownloadedFile? {
        val remoteFile = holder.get(url) ?: return null
        val file = getFileById(remoteFile.id)
        return if (file.exists() && file.isFile) {
            DownloadedFile(remoteFile, file)
        } else {
            null
        }
    }
}

private fun ResponseBody.copyToWithProgress(destinationFile: File): Flow<Int> {
    return byteStream().copyToWithProgress(destinationFile.outputStream(), contentLength())
}

private fun InputStream.copyToWithProgress(
    outputStream: OutputStream,
    length: Long,
): Flow<Int> = flow {
    emit(0)
    use { input ->
        outputStream.use { output ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var progressBytes = 0L
            var bytes = input.read(buffer)
            while (bytes >= 0) {
                output.write(buffer, 0, bytes)
                progressBytes += bytes
                bytes = input.read(buffer)
                emit(((progressBytes * 100) / length).toInt())
            }
        }
    }
    emit(100)

}
    .flowOn(Dispatchers.IO)
    .distinctUntilChanged()