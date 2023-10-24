package ru.radiationx.data.downloader

import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import ru.radiationx.data.DataPreferences
import ru.radiationx.data.datasource.SuspendMutableStateFlow
import ru.radiationx.data.entity.domain.types.ReleaseId
import java.util.UUID
import javax.inject.Inject

class RemoteFileStorage @Inject constructor(
    @DataPreferences private val sharedPreferences: SharedPreferences,
    private val moshi: Moshi,
) : RemoteFileHolder {

    companion object {
        private const val REMOTE_FILES_KEY = "data.remote_files"
        private const val BUCKET_APP_UPDATES = "app_updates"
        private const val BUCKET_TORRENT = "torrent"
    }

    private val fileNameRegex = Regex(
        "filename\\s*=\\s*[\\\"]?([\\s\\S]*?)[\\\"]?(?:\\;|\\n|\$)",
        RegexOption.IGNORE_CASE
    )

    private val dataAdapter by lazy {
        val type = Types.newParameterizedType(List::class.java, RemoteFileDb::class.java)
        moshi.adapter<List<RemoteFileDb>>(type)
    }

    private val remoteFilesState = SuspendMutableStateFlow {
        loadAll()
    }

    override fun generateId(): RemoteFileId {
        return RemoteFileId(UUID.randomUUID())
    }

    override suspend fun get(url: String): RemoteFile? {
        return remoteFilesState.getValue()
            .find { it.url == url }
            ?.toDomain()
    }

    override suspend fun put(data: RemoteFileSaveData): RemoteFile {
        val newRemoteFile = data.toDb()
        remoteFilesState.update { remoteFiles ->
            val mutableRemoteFiles = remoteFiles.toMutableList()
            mutableRemoteFiles
                .find { it.id == newRemoteFile.id }
                ?.also { mutableRemoteFiles.remove(it) }
            mutableRemoteFiles.add(newRemoteFile)
            mutableRemoteFiles
        }
        saveAll()
        return newRemoteFile.toDomain()
    }

    private suspend fun saveAll() {
        withContext(Dispatchers.IO) {
            val jsonEpisodes = remoteFilesState.getValue()
                .let { dataAdapter.toJson(it) }
            sharedPreferences
                .edit()
                .putString(REMOTE_FILES_KEY, jsonEpisodes)
                .apply()
        }
    }

    private suspend fun loadAll(): List<RemoteFileDb> {
        return withContext(Dispatchers.IO) {
            sharedPreferences
                .getString(REMOTE_FILES_KEY, null)
                ?.let { dataAdapter.fromJson(it) }
                .orEmpty()
        }
    }

    private fun RemoteFileSaveData.toDb(): RemoteFileDb {
        val bucketName = when (bucket) {
            RemoteFile.Bucket.AppUpdates -> BUCKET_APP_UPDATES
            is RemoteFile.Bucket.Torrent -> BUCKET_TORRENT
        }
        val bucketReleaseId = when (bucket) {
            RemoteFile.Bucket.AppUpdates -> null
            is RemoteFile.Bucket.Torrent -> bucket.id
        }
        return RemoteFileDb(
            id = id.id.toString(),
            url = url,
            bucketName = bucketName,
            bucketReleaseId = bucketReleaseId?.id,
            contentDisposition = contentDisposition,
            contentType = contentType
        )
    }

    private fun RemoteFileDb.toDomain(): RemoteFile {
        val bucket = calculateBucket()
        return RemoteFile(
            id = RemoteFileId(UUID.fromString(id)),
            url = url,
            bucket = bucket,
            name = calculateName(bucket),
            mimeType = calculateMimeType(bucket)
        )
    }

    private fun RemoteFileDb.calculateBucket(): RemoteFile.Bucket {
        val releaseId = bucketReleaseId?.let { ReleaseId(it) }
        return when (bucketName) {
            BUCKET_APP_UPDATES -> RemoteFile.Bucket.AppUpdates
            BUCKET_TORRENT -> {
                requireNotNull(releaseId) {
                    "ReleaseId is null for $bucketName"
                }
                RemoteFile.Bucket.Torrent(releaseId)
            }

            else -> throw IllegalArgumentException("Unknown bucket name $bucketName")
        }
    }

    private fun RemoteFileDb.calculateName(bucket: RemoteFile.Bucket): String {
        if (contentDisposition != null) {
            fileNameRegex.find(contentDisposition)?.also {
                return it.groupValues[1]
            }
        }
        url.toHttpUrl().pathSegments.lastOrNull()?.also {
            return it
        }
        val extension = when (bucket) {
            RemoteFile.Bucket.AppUpdates -> "apk"
            is RemoteFile.Bucket.Torrent -> "torrent"
        }
        return "Unknown($id).$extension"
    }

    private fun RemoteFileDb.calculateMimeType(bucket: RemoteFile.Bucket): String {
        if (contentType != null) {
            return contentType
        }
        return when (bucket) {
            RemoteFile.Bucket.AppUpdates -> "application/vnd.android.package-archive"
            is RemoteFile.Bucket.Torrent -> "application/x-bittorrent"
        }
    }

}