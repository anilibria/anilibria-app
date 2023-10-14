package ru.radiationx.anilibria.ui.common

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.utils.messages.SystemMessage
import ru.radiationx.anilibria.utils.messages.SystemMessenger
import javax.inject.Inject

class ScreenMessagesObserver @Inject constructor(
    private val context: Context,
    private val screenMessenger: SystemMessenger,
) : DefaultLifecycleObserver {

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

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        messengerJob?.cancel()
        messengerJob = messageBufferTrigger
            .map { messagesBuffer.toList() }
            .onEach { messagesBuffer.clear() }
            .flatMapConcat { it.asFlow() }
            .onEach { message ->
                showMessage(message)
            }
            .launchIn(scope)
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        messengerJob?.cancel()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        scope.cancel()
    }

    private fun showMessage(message: SystemMessage) {
        context.also {
            Toast.makeText(it, message.message, Toast.LENGTH_SHORT).show()
        }
    }
}