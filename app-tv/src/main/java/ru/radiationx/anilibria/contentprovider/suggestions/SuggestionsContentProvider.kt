package ru.radiationx.anilibria.contentprovider.suggestions

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.contentprovider.SystemSuggestionEntity
import ru.radiationx.data.entity.app.search.SuggestionItem
import ru.radiationx.data.repository.SearchRepository
import ru.radiationx.shared_app.di.DI

class SuggestionsContentProvider : ContentProvider() {

    companion object {

        const val INTENT_ACTION = "GLOBALSEARCH"

        private val queryProjection = SystemSuggestionEntity.projection + arrayOf(
            SearchManager.SUGGEST_COLUMN_INTENT_ACTION,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID
        )
        private const val TAG = "SearchContentProvider"
        private const val AUTHORITY = "ru.radiationx.anilibria.contentprovider.suggestions"
        private const val SEARCH_SUGGEST = 1
    }

    private val uriMatcher by lazy { buildUriMatcher() }

    private val searchRepository by lazy { DI.get(SearchRepository::class.java) }

    override fun onCreate(): Boolean {
        return true
    }

    @SuppressLint("CheckResult")
    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        App.appCreateAction.filter { it }.blockingFirst()

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
        selectionArgs: Array<out String>?
    ): Int {
        throw UnsupportedOperationException("update is not implemented.")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        throw UnsupportedOperationException("delete is not implemented.")
    }

    private fun search(query: String): Cursor {
        val result = searchRepository.fastSearch(query).blockingGet()
        val matrixCursor = MatrixCursor(queryProjection)
        result.forEach {
            val entity = it.convertToEntity()
            val columns = appendProjectionColumns(entity.id, entity.getRow())
            matrixCursor.addRow(columns)
        }
        return matrixCursor
    }

    private fun appendProjectionColumns(id: Int, columns: Array<Any?>): Array<Any?> =
        columns + INTENT_ACTION + id

    private fun SuggestionItem.convertToEntity() = SystemSuggestionEntity(
        id,
        names.joinToString(),
        -1,
        -1,
        cardImage = poster
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