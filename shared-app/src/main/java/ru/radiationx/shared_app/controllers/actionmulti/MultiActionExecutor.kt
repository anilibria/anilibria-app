package ru.radiationx.shared_app.controllers.actionmulti

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.radiationx.shared.ktx.coRunCatching
import timber.log.Timber

class MultiActionExecutor<KEY, ARG, RESULT>(
    private val coroutineScope: CoroutineScope,
    private val onSuccess: (suspend (MultiEventSuccess<KEY, ARG, RESULT>) -> Unit)? = null,
    private val onError: (suspend (MultiEventError<KEY, ARG>) -> Unit)? = null,
    private val action: suspend (MultiActionParams<KEY, ARG>) -> RESULT
) {

    private val actionJobs = mutableMapOf<KEY, Job>()

    private val _state = MutableStateFlow<Map<KEY, ARG>>(emptyMap())

    fun observeState(): StateFlow<Map<KEY, ARG>> {
        return _state
    }

    fun execute(key: KEY, arg: ARG) {
        val actionJob = actionJobs[key]
        if (actionJob?.isActive == true) {
            return
        }
        actionJob?.cancel()
        actionJobs[key] = coroutineScope.launch {
            _state.update {
                it.plus(key to arg)
            }
            coRunCatching {
                action.invoke(MultiActionParams(key, arg))
            }.onSuccess { data ->
                val event = MultiEventSuccess(key, arg, data)
                onSuccess?.invoke(event)
            }.onFailure { error ->
                Timber.e(error, "key=$key, arg=$arg")
                val event = MultiEventError(key, arg, error)
                onError?.invoke(event)
            }
            _state.update { it.minus(key) }
        }
    }
}

