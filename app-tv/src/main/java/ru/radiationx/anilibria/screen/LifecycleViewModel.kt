package ru.radiationx.anilibria.screen

import androidx.annotation.CallSuper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel

open class LifecycleViewModel : ViewModel(), DefaultLifecycleObserver {

    private var created = false


    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        onCreate()
    }

    @CallSuper
    protected open fun onCreate() {
        if (!created) {
            created = true
            onColdCreate()
        }
    }

    protected open fun onColdCreate() {}

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        onStart()
    }

    protected open fun onStart() {
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        onResume()
    }

    protected open fun onResume() {
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        onPause()
    }

    protected open fun onPause() {
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        onStop()
    }

    protected open fun onStop() {
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        onDestroy()
    }

    protected open fun onDestroy() {
    }
}