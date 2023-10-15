package ru.radiationx.shared_app.analytics

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import ru.radiationx.data.analytics.TimeCounter

class LifecycleTimeCounter(
    private val onDestroyTimeListener: (Long) -> Unit = {},
) : DefaultLifecycleObserver {

    private val timeCounter by lazy { TimeCounter() }

    val value: Long
        get() = timeCounter.elapsed()

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        timeCounter.start()
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        timeCounter.resume()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        timeCounter.pause()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        onDestroyTimeListener(value)
    }
}