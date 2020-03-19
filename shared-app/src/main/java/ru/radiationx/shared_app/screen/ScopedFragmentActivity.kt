package ru.radiationx.shared_app.screen

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.fragment.app.FragmentActivity
import ru.radiationx.shared_app.di.DependencyInjector
import ru.radiationx.shared_app.di.ScopeProvider
import ru.radiationx.shared_app.di.ActivityScopeCloseChecker
import toothpick.smoothie.lifecycle.closeOnDestroy

open class ScopedFragmentActivity(@LayoutRes layoutId: Int = 0) : FragmentActivity(layoutId),
    ScopeProvider {

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