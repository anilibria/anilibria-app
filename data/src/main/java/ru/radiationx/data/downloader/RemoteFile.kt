package ru.radiationx.data.downloader

import ru.radiationx.data.entity.domain.types.ReleaseId

data class RemoteFile(
    val id: RemoteFileId,
    val url: String,
    val bucket: Bucket,
    val name: String,
    val mimeType: String,
) {
    sealed class Bucket {
        data object AppUpdates : Bucket()
        data class Torrent(val id: ReleaseId) : Bucket()
    }
}
