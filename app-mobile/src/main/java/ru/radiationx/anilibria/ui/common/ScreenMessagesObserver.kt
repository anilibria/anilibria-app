package ru.radiationx.anilibria.ui.common

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposables
import io.reactivex.subjects.BehaviorSubject
import ru.radiationx.anilibria.utils.messages.SystemMessage
import ru.radiationx.anilibria.utils.messages.SystemMessenger
import ru.radiationx.shared.ktx.addTo
import javax.inject.Inject


class ScreenMessagesObserver @Inject constructor(
        private val context: Context,
        private val screenMessenger: SystemMessenger
) : LifecycleObserver {

    private val disposables = CompositeDisposable()
    private var messengerDisposable = Disposables.disposed()
    private val messageBufferTrigger = BehaviorSubject.create<Boolean>()
    private val messagesBuffer = mutableListOf<SystemMessage>()

    init {
        screenMessenger
                .observe()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    messagesBuffer.add(it)
                    messageBufferTrigger.onNext(true)
                }
                .addTo(disposables)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun resume() {
        messengerDisposable = messageBufferTrigger
                .flatMapIterable { messagesBuffer.toList() }
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { message ->
                    showMessage(message)
                    messagesBuffer.clear()
                }
                .addTo(disposables)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun pause() {
        messengerDisposable.dispose()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        disposables.dispose()
    }

    private fun showMessage(message: SystemMessage) {
        context.also {
            Toast.makeText(it, message.message, Toast.LENGTH_SHORT).show()
        }
    }
}