package ru.radiationx.shared_app.controllers.loadersingle

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.radiationx.shared.ktx.SerialJob
import ru.radiationx.shared.ktx.coRunCatching
import timber.log.Timber

class SingleLoader<DATA>(
    private val coroutineScope: CoroutineScope,
    private val dataSource: suspend () -> DATA
) {

    private val loadingJob = SerialJob()

    private val _state = MutableStateFlow(SingleLoaderState<DATA>())
    val state = _state.asStateFlow()

    private val _actionSuccess = MutableSharedFlow<DATA>()
    val actionSuccess = _actionSuccess.asSharedFlow()

    private val _actionError = MutableSharedFlow<Throwable>()
    val actionError = _actionError.asSharedFlow()

    fun cancelLoading() {
        loadingJob.cancel()
    }

    fun reset() {
        loadingJob.cancel()
        _state.value = SingleLoaderState()
    }

    fun modifyData(data: DATA?) {
        _state.update { it.copy(data = data) }
    }

    fun modifyData(block: (DATA) -> DATA) {
        val newData = state.value.data?.let(block)
        modifyData(newData)
    }

    fun refresh() {
        loadingJob.launch(coroutineScope) {
            _state.update { it.copy(loading = true) }
            coRunCatching {
                dataSource.invoke()
            }.onSuccess { data ->
                _state.update { it.copy(data = data, error = null) }
                _actionSuccess.emit(data)
            }.onFailure { error ->
                Timber.e(error)
                _state.update { it.copy(data = null, error = error) }
                _actionError.emit(error)
            }
            _state.update { it.copy(loading = false) }
        }
    }
}
