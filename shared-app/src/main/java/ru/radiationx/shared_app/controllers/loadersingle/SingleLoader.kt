package ru.radiationx.shared_app.controllers.loadersingle

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import ru.radiationx.shared.ktx.SerialJob
import ru.radiationx.shared.ktx.coRunCatching
import timber.log.Timber

class SingleLoader<DATA>(
    private val coroutineScope: CoroutineScope,
    private val dataSource: suspend () -> DATA
) {

    private val loadingJob = SerialJob()

    private val _state = MutableStateFlow(SingleLoaderState.empty<DATA>())

    fun observeState(): StateFlow<SingleLoaderState<DATA>> {
        return _state
    }

    fun cancel() {
        loadingJob.cancel()
    }

    fun reset() {
        loadingJob.cancel()
        _state.value = SingleLoaderState.empty()
    }

    fun modifyData(data: DATA?) {
        _state.update { it.copy(data = data) }
    }

    fun modifyData(block: (DATA) -> DATA) {
        val newData = _state.value.data?.let(block)
        modifyData(newData)
    }

    fun isNeedRefresh(): Boolean {
        return _state.value.error != null || _state.value.data == null
    }

    fun refresh() {
        loadingJob.launch(coroutineScope) {
            _state.update { it.copy(loading = true) }
            coRunCatching {
                dataSource.invoke()
            }.onSuccess { data ->
                _state.update { it.copy(data = data, error = null) }
            }.onFailure { error ->
                Timber.e(error)
                _state.update { it.copy(data = null, error = error) }
            }
            _state.update { it.copy(loading = false) }
        }
    }
}
