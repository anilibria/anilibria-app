package ru.radiationx.shared_app.screen

import android.os.Bundle
import androidx.annotation.LayoutRes
import ru.radiationx.shared.ktx.android.attachBackPressed
import ru.radiationx.shared_app.di.FlowNavigationModule
import ru.radiationx.shared_app.R
import ru.radiationx.shared_app.navigation.ScopedAppNavigator
import ru.radiationx.shared_app.di.getDependency
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import javax.inject.Inject

open class FlowFragment(@LayoutRes layoutId: Int = R.layout.fragment_tab_root) : ScopedFragment(layoutId) {

    private val parentRouter by lazy { getDependency(Router::class.java, dependencyInjector.parentScopeTag) }
    private val navigatorLocal by lazy { FlowNavigator(this, parentRouter) }

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var navigatorHolder: NavigatorHolder

    override fun onCreate(savedInstanceState: Bundle?) {
        dependencyInjector.installModules(FlowNavigationModule(parentRouter))
        super.onCreate(savedInstanceState)
        attachBackPressed {
            router.exit()
        }
    }

    override fun onResume() {
        super.onResume()
        navigatorHolder.setNavigator(navigatorLocal)
    }

    override fun onPause() {
        navigatorHolder.removeNavigator()
        super.onPause()
    }

    private class FlowNavigator(fragment: FlowFragment, private val router: Router) : ScopedAppNavigator(
        fragment.requireActivity(),
        R.id.flowFragmentsContainer,
        fragment.childFragmentManager,
        fragment
    ) {

        override fun activityBack() {
            router.exit()
        }
    }
}