package ru.radiationx.shared_app

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.fragment.app.FragmentActivity
import toothpick.smoothie.lifecycle.closeOnDestroy

open class BaseFragmentActivity(@LayoutRes layoutId: Int = 0) : FragmentActivity(layoutId), ScopeProvider {

    protected val dependencyInjector by lazy { DependencyInjector(intent.extras) }

    private val scopeCloseChecker by lazy { ActivityScopeCloseChecker(this) }

    override val screenScopeTag: String
        get() = dependencyInjector.screenScopeTag

    override fun onCreate(savedInstanceState: Bundle?) {
        dependencyInjector.onCreate(this, savedInstanceState).closeOnDestroy(this)
        super.onCreate(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        dependencyInjector.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (scopeCloseChecker.needCloseScope()) {
            dependencyInjector.closeScope()
        }
    }
}