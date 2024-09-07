package ru.radiationx.shared_app.controllers.loadersearch

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import ru.radiationx.shared_app.controllers.loadersingle.SingleLoader

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
    val queryState = _queryState.asStateFlow()

    val loadingState = loader.state
    val actionSuccess = loader.actionSuccess
    val actionError = loader.actionError

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

