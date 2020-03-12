package ru.radiationx.anilibria.common.fragment

import android.os.Bundle
import android.widget.Toast
import androidx.leanback.app.GuidedStepSupportFragment
import ru.radiationx.shared.ktx.android.attachBackPressed
import ru.radiationx.shared_app.di.DependencyInjector
import ru.radiationx.shared_app.di.FragmentScopeCloseChecker
import ru.radiationx.shared_app.di.ScopeProvider
import ru.radiationx.shared_app.di.getScopedDependency
import toothpick.smoothie.lifecycle.closeOnDestroy
import javax.inject.Inject

open class BaseGuidedStepFragment : GuidedStepFragmentHelper.AppGuidedStepSupportFragment(), ScopeProvider {

    protected val dependencyInjector by lazy { DependencyInjector(arguments) }

    private val scopeCloseChecker by lazy { FragmentScopeCloseChecker(this) }

    override val screenScopeTag: String
        get() = dependencyInjector.screenScopeTag

    override fun onCreate(savedInstanceState: Bundle?) {
        dependencyInjector.onCreate(this, savedInstanceState).closeOnDestroy(this)
        super.onCreate(savedInstanceState)
        attachBackPressed {
            if (isEnabled) {
                getScopedDependency(DialogRouter::class.java).exit()
                isEnabled = false
            }
        }
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