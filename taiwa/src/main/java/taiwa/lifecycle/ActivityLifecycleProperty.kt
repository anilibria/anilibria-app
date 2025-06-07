package taiwa.lifecycle

import android.app.Activity
import android.app.Application
import android.os.Bundle
import kotlin.reflect.KProperty

fun <A : Activity, T : Destroyable> A.lifecycleLazy(
    creator: (A) -> T,
): LifecycleProperty<A, T> {
    return ActivityLifecycleProperty(creator)
}

private class ActivityLifecycleProperty<in A : Activity, T : Destroyable>(
    creator: (A) -> T,
) : LifecycleProperty<A, T>(creator) {

    private var lifecycleCallbacks: Application.ActivityLifecycleCallbacks? = null
    private var activity: Activity? by weakReference(null)

    override fun getValue(thisRef: A, property: KProperty<*>): T {
        return super.getValue(thisRef, property)
            .also { registerLifecycleCallbacksIfNeeded(thisRef) }
    }

    private fun registerLifecycleCallbacksIfNeeded(activity: Activity) {
        if (lifecycleCallbacks != null) return
        this.activity = activity
        ActivityLifecycleCallbacks()
            .also { callbacks -> this.lifecycleCallbacks = callbacks }
            .let(activity.application::registerActivityLifecycleCallbacks)
    }

    override fun clear() {
        super.clear()
        val lifecycleCallbacks = lifecycleCallbacks
        if (lifecycleCallbacks != null) {
            activity?.application?.unregisterActivityLifecycleCallbacks(lifecycleCallbacks)
        }

        this.activity = null
        this.lifecycleCallbacks = null
    }

    private inner class ActivityLifecycleCallbacks :
        Application.ActivityLifecycleCallbacks {

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        }

        override fun onActivityStarted(activity: Activity) {
        }

        override fun onActivityResumed(activity: Activity) {
        }

        override fun onActivityPaused(activity: Activity) {
        }

        override fun onActivityStopped(activity: Activity) {
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        }

        override fun onActivityDestroyed(activity: Activity) {
            if (activity === this@ActivityLifecycleProperty.activity) clear()
        }
    }
}