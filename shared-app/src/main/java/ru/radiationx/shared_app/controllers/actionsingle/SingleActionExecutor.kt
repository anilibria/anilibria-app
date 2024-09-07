package ru.radiationx.shared_app.controllers.actionsingle

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.radiationx.shared.ktx.coRunCatching
import timber.log.Timber

class SingleActionExecutor<ARG, RESULT>(
    private val coroutineScope: CoroutineScope,
    private val onSuccess: (suspend (SingleEventSuccess<ARG, RESULT>) -> Unit)? = null,
    private val onError: (suspend (SingleEventError<ARG>) -> Unit)? = null,
    private val action: suspend (ARG) -> RESULT
) {

    private var actionJob: Job? = null

    private val _state = MutableStateFlow<ARG?>(null)
    val state = _state.asStateFlow()

    private val _actionSuccess = MutableSharedFlow<SingleEventSuccess<ARG, RESULT>>()
    val actionSuccess = _actionSuccess.asSharedFlow()

    private val _actionError = MutableSharedFlow<SingleEventError<ARG>>()
    val actionError = _actionError.asSharedFlow()

    fun execute(arg: ARG) {
        if (actionJob?.isActive == true) {
            return
        }
        actionJob?.cancel()
        actionJob = coroutineScope.launch {
            _state.value = arg
            coRunCatching {
                action.invoke(arg)
            }.onSuccess { data ->
                val event = SingleEventSuccess(arg, data)
                onSuccess?.invoke(event)
                _actionSuccess.emit(event)
            }.onFailure { error ->
                Timber.e("arg=$arg", error)
                val event = SingleEventError(arg, error)
                onError?.invoke(event)
                _actionError.emit(event)
            }
            _state.value = null
        }
    }
}

