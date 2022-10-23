package ru.radiationx.shared_app.analytics

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import ru.radiationx.data.analytics.TimeCounter

class LifecycleTimeCounter(
    private val onDestroyTimeListener: (Long) -> Unit = {}
) : LifecycleObserver {

    private val timeCounter by lazy { TimeCounter() }

    val value: Long
        get() = timeCounter.elapsed()

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        timeCounter.start()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun resume() {
        timeCounter.resume()

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun pause() {
        timeCounter.pause()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        onDestroyTimeListener(value)
    }
}