package ru.radiationx.anilibria.ui.common

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ru.radiationx.anilibria.utils.messages.SystemMessage
import ru.radiationx.anilibria.utils.messages.SystemMessenger
import javax.inject.Inject

class ScreenMessagesObserver @Inject constructor(
    private val context: Context,
    private val screenMessenger: SystemMessenger
) : LifecycleObserver {

    private val messageBufferTrigger = MutableSharedFlow<Boolean>()
    private val messagesBuffer = mutableListOf<SystemMessage>()
    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    private var messengerJob: Job? = null

    init {
        screenMessenger
            .observe()
            .onEach {
                messagesBuffer.add(it)
                messageBufferTrigger.emit(true)
            }
            .launchIn(scope)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun resume() {
        messengerJob?.cancel()
        messengerJob = messageBufferTrigger
            .map { messagesBuffer.toList() }
            .onEach { messagesBuffer.clear() }
            .flatMapConcat { it.asFlow() }
            .distinctUntilChanged()
            .onEach { message ->
                showMessage(message)
            }
            .launchIn(scope)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun pause() {
        messengerJob?.cancel()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        scope.cancel()
    }

    private fun showMessage(message: SystemMessage) {
        context.also {
            Toast.makeText(it, message.message, Toast.LENGTH_SHORT).show()
        }
    }
}