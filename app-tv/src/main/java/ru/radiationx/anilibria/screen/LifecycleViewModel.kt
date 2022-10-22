package ru.radiationx.anilibria.screen

import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModel

open class LifecycleViewModel : ViewModel(), LifecycleObserver {

    private var created = false

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    @CallSuper
    protected open fun onCreate() {
        if (!created) {
            created = true
            onColdCreate()
        }
    }

    protected open fun onColdCreate() {}

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    protected open fun onStart() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    protected open fun onResume() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    protected open fun onPause() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    protected open fun onStop() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    protected open fun onDestroy() {
    }
}