package ru.radiationx.shared_app.screen

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import ru.radiationx.shared_app.di.DependencyInjector
import ru.radiationx.shared_app.di.FragmentScopeCloseChecker
import ru.radiationx.shared_app.di.ScopeProvider
import toothpick.smoothie.lifecycle.closeOnDestroy


open class BaseFragment(@LayoutRes layoutId: Int = 0) : Fragment(layoutId), ScopeProvider {

    protected val dependencyInjector by lazy { DependencyInjector(arguments) }

    private val scopeCloseChecker by lazy { FragmentScopeCloseChecker(this) }

    override val screenScopeTag: String
        get() = dependencyInjector.screenScopeTag

    override fun onCreate(savedInstanceState: Bundle?) {
        dependencyInjector.onCreate(this, savedInstanceState).closeOnDestroy(this)
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        scopeCloseChecker.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        scopeCloseChecker.onSaveInstanceState()
        dependencyInjector.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (scopeCloseChecker.needCloseScope()) {
            dependencyInjector.closeScope()
        }
    }
}
