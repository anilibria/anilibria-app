package ru.radiationx.data.historyfile

import android.content.Context
import android.net.Uri
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.buffer
import okio.sink
import okio.source
import ru.radiationx.data.datasource.holders.EpisodesCheckerHolder
import ru.radiationx.data.datasource.holders.HistoryHolder
import ru.radiationx.data.datasource.holders.ReleaseUpdateHolder
import ru.radiationx.data.downloader.LocalFile
import ru.radiationx.data.historyfile.mapper.toDomain
import ru.radiationx.data.historyfile.mapper.toExport
import ru.radiationx.data.historyfile.models.HistoryExport
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

class HistoryFileRepository @Inject constructor(
    private val context: Context,
    private val historyHolder: HistoryHolder,
    private val releaseHolder: ReleaseUpdateHolder,
    private val episodesHolder: EpisodesCheckerHolder,
    private val moshi: Moshi,
) {

    private val dataAdapter by lazy {
        moshi.adapter(HistoryExport::class.java)
    }

    suspend fun exportFile(): LocalFile {
        return withContext(Dispatchers.IO) {
            val data = HistoryExport(
                history = historyHolder.getIds().map { it.toExport() },
                updates = releaseHolder.getReleases().map { it.toExport() },
                episodes = episodesHolder.getEpisodes().map { it.toExport() }
            )
            val date = SimpleDateFormat("ddMMyyyyHHmm").format(Date())

            val file = File(getCacheDir(),"anilibria_history_${date}.json")
            FileOutputStream(file).sink().buffer().use {
                dataAdapter.toJson(it, data)
            }
            LocalFile(file, file.name, "application/json")
        }
    }

    suspend fun importFile(uri: Uri) {
        withContext(Dispatchers.IO) {
            val data = context.contentResolver.openFileDescriptor(uri, "r").use { descriptor ->
                requireNotNull(descriptor) {
                    "File descriptor is null"
                }
                FileInputStream(descriptor.fileDescriptor).source().buffer().use {
                    dataAdapter.fromJson(it)
                }
            }
            requireNotNull(data) {
                "Readed data by file is null"
            }
            historyHolder.putAllIds(data.history.map { it.toDomain() })
            releaseHolder.putAllRelease(data.updates.map { it.toDomain() })
            episodesHolder.putAllEpisode(data.episodes.map { it.toDomain() })
        }
    }

    private fun getCacheDir(): File {
        val file = File(context.cacheDir, "anilibria_export")
        file.mkdir()
        return file
    }
}