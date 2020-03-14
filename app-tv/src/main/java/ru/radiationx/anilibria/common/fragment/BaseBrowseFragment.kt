package ru.radiationx.anilibria.common.fragment

import android.os.Bundle
import androidx.leanback.app.BrowseSupportFragment
import ru.radiationx.shared.ktx.android.attachBackPressed
import ru.radiationx.shared_app.di.DependencyInjector
import ru.radiationx.shared_app.di.FragmentScopeCloseChecker
import ru.radiationx.shared_app.di.ScopeProvider
import ru.radiationx.shared_app.di.getScopedDependency
import toothpick.smoothie.lifecycle.closeOnDestroy

open class BaseBrowseFragment : BrowseSupportFragment(), ScopeProvider {

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