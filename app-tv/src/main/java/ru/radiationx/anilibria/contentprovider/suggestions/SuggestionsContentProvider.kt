package ru.radiationx.anilibria.contentprovider.suggestions

import android.app.SearchManager
import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.contentprovider.SystemSuggestionEntity
import ru.radiationx.data.api.releases.ReleaseRepository
import ru.radiationx.data.api.releases.models.Release
import ru.radiationx.data.app.config.ApiConfig
import ru.radiationx.quill.Quill

class SuggestionsContentProvider : ContentProvider() {

    companion object {

        const val INTENT_ACTION = "GLOBALSEARCH"

        private val queryProjection = SystemSuggestionEntity.projection + arrayOf(
            SearchManager.SUGGEST_COLUMN_INTENT_ACTION,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID
        )
        private const val AUTHORITY = "ru.radiationx.anilibria.contentprovider.suggestions"
        private const val SEARCH_SUGGEST = 1
    }

    private val uriMatcher by lazy { buildUriMatcher() }

    private val releaseRepository by lazy { Quill.getRootScope().get(ReleaseRepository::class) }
    private val apiConfig by lazy { Quill.getRootScope().get(ApiConfig::class) }

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor {
        runBlocking { App.appCreateAction.filter { it }.first() }

        return if (uriMatcher.match(uri) == SEARCH_SUGGEST) {
            search(uri.lastPathSegment.orEmpty())
        } else {
            throw IllegalArgumentException("Unknown Uri: $uri")
        }
    }

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw UnsupportedOperationException("insert is not implemented.")
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int {
        throw UnsupportedOperationException("update is not implemented.")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        throw UnsupportedOperationException("delete is not implemented.")
    }

    private fun search(query: String): Cursor {
        val result = runBlocking { releaseRepository.search(query) }
        val matrixCursor = MatrixCursor(queryProjection)
        result.items.forEach {
            val entity = it.convertToEntity(apiConfig)
            val columns = appendProjectionColumns(entity.id, entity.getRow())
            matrixCursor.addRow(columns)
        }
        return matrixCursor
    }

    private fun appendProjectionColumns(id: Int, columns: Array<Any?>): Array<Any?> =
        columns + INTENT_ACTION + id

    private fun Release.convertToEntity(apiConfig: ApiConfig) = SystemSuggestionEntity(
        id = id.id,
        title = listOf(names.main, names.english).joinToString(),
        duration = -1,
        productionYear = -1,
        cardImage = poster?.withBase(apiConfig.baseImagesUrl)
    )

    private fun buildUriMatcher(): UriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(
            AUTHORITY,
            "/search/${SearchManager.SUGGEST_URI_PATH_QUERY}",
            SEARCH_SUGGEST
        )
        addURI(
            AUTHORITY,
            "/search/${SearchManager.SUGGEST_URI_PATH_QUERY}/*",
            SEARCH_SUGGEST
        )
    }
}