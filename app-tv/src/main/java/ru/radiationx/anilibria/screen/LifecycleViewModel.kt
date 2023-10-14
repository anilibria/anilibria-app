package ru.radiationx.anilibria.screen

import androidx.annotation.CallSuper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel

open class LifecycleViewModel : ViewModel(), DefaultLifecycleObserver {

    private var coldCreated = false
    private var coldStarted = false
    private var coldResumed = false

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        onCreate()
        //Log.d("kekeke", "${this::class.simpleName} onCreate")
    }

    @CallSuper
    protected open fun onCreate() {
        if (!coldCreated) {
            coldCreated = true
            onColdCreate()
        }
    }

    protected open fun onColdCreate() {}

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        //Log.d("kekeke", "${this::class.simpleName} onStart")
        onStart()
        if (!coldStarted) {
            coldStarted = true
            onColdStart()
        }
    }

    protected open fun onStart() {
    }

    protected open fun onColdStart() {

    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
       // Log.d("kekeke", "${this::class.simpleName} onResume")
        onResume()
        if (!coldResumed) {
            coldResumed = true
            onColdResume()
        }
    }

    protected open fun onResume() {
    }

    protected open fun onColdResume() {
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