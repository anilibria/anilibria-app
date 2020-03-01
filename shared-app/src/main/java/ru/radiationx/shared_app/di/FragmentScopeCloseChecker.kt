package ru.radiationx.shared_app.di

import androidx.fragment.app.Fragment

class FragmentScopeCloseChecker(private val fragment: Fragment) : ScopeCloseChecker {

    protected var instanceStateSaved: Boolean = false

    // It will be valid only for 'onDestroy()' method
    override fun needCloseScope(): Boolean = when {
        fragment.activity?.isChangingConfigurations == true -> false
        fragment.activity?.isFinishing == true -> true
        else -> isRealRemove()
    }

    fun onResume() {
        instanceStateSaved = false
    }

    fun onSaveInstanceState() {
        instanceStateSaved = true
    }

    // This is android, baby!
    private fun isRealRemove(): Boolean = fragment.run {
        (isRemoving && !instanceStateSaved) || // Because isRemoving == true for fragment in backstack on screen rotation
                ((parentFragment as? ScopeCloseChecker)?.needCloseScope() ?: false)
    }
}