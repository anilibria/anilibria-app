package ru.radiationx.data.app.downloader

import ru.radiationx.data.app.downloader.models.RemoteFile
import ru.radiationx.data.app.downloader.models.RemoteFileId
import ru.radiationx.data.app.downloader.models.RemoteFileSaveData

interface RemoteFileHolder {

    fun generateId(): RemoteFileId

    suspend fun getSize(): Int

    suspend fun get(url: String): RemoteFile?

    suspend fun put(data: RemoteFileSaveData): RemoteFile
}