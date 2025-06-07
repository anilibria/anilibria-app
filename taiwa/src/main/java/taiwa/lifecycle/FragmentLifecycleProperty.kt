package taiwa.lifecycle

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import kotlin.reflect.KProperty

fun <F : Fragment, T : Destroyable> Fragment.lifecycleLazy(
    creator: (F) -> T,
): LifecycleProperty<F, T> {
    return FragmentLifecycleProperty(creator)
}

internal class FragmentLifecycleProperty<F : Fragment, T : Destroyable>(
    creator: (F) -> T,
) : LifecycleProperty<F, T>(creator) {

    private var lifecycleCallbacks: FragmentManager.FragmentLifecycleCallbacks? = null
    private var fragmentManager: FragmentManager? = null

    override fun getValue(thisRef: F, property: KProperty<*>): T {
        val value = super.getValue(thisRef, property)
        registerLifecycleCallbacksIfNeeded(thisRef)
        return value
    }

    private fun registerLifecycleCallbacksIfNeeded(fragment: Fragment) {
        if (lifecycleCallbacks != null) return

        val fragmentManager = fragment.parentFragmentManager
        this.fragmentManager = fragmentManager
        lifecycleCallbacks = FragmentLifecycleCallback(fragment).also { callbacks ->
            fragmentManager.registerFragmentLifecycleCallbacks(callbacks, false)
        }
    }

    override fun clear() {
        super.clear()

        val lifecycleCallbacks = lifecycleCallbacks
        if (lifecycleCallbacks != null) {
            fragmentManager?.unregisterFragmentLifecycleCallbacks(lifecycleCallbacks)
        }

        fragmentManager = null
        this.lifecycleCallbacks = null
    }

    private inner class FragmentLifecycleCallback(
        fragment: Fragment,
    ) : FragmentManager.FragmentLifecycleCallbacks() {

        private val fragment by weakReference(fragment)

        override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
            if (fragment === f) clear()
        }
    }
}