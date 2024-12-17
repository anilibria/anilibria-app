package ru.radiationx.shared_app.controllers.loadersearch

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.shared_app.controllers.loadersingle.SingleLoader
import ru.radiationx.shared_app.controllers.loadersingle.SingleLoaderState

class SearchLoader<QUERY : SearchQuery, DATA>(
    private val coroutineScope: CoroutineScope,
    private val dataSource: suspend (QUERY) -> DATA
) {

    private companion object {
        const val QUERY_DEBOUNCE = 300L
    }

    private val loader = SingleLoader(coroutineScope) {
        val query = _queryState.filterNotNull().first()
        dataSource.invoke(query)
    }

    private val _queryState = MutableStateFlow<QUERY?>(null)

    fun observeQuery(): StateFlow<QUERY?> {
        return _queryState
    }

    fun observeState(): StateFlow<SingleLoaderState<DATA>> {
        return loader.observeState()
    }

    fun getQuery(): QUERY? {
        return _queryState.value
    }

    init {
        _queryState
            .drop(1)
            .debounce(QUERY_DEBOUNCE)
            .distinctUntilChanged()
            .onEach { query ->
                if (query?.isEmpty() != false) {
                    loader.reset()
                } else {
                    loader.refresh()
                }
            }
            .launchIn(coroutineScope)
    }

    fun refresh() {
        loader.refresh()
    }

    fun onNewQuery(query: QUERY?) {
        _queryState.value = query
    }
}

@Suppress("FunctionName")
fun <T> StringSearchLoader(
    coroutineScope: CoroutineScope,
    dataSource: suspend (StringQuery) -> T
) = SearchLoader(coroutineScope, dataSource)

