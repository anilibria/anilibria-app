package ru.radiationx.anilibria.ui.fragments


import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.ArcMotion
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialSharedAxis
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.di.MessengerModule
import ru.radiationx.anilibria.di.RouterModule
import ru.radiationx.anilibria.navigation.BaseFragmentScreen
import ru.radiationx.anilibria.presentation.common.ILinkHandler
import ru.radiationx.anilibria.ui.common.BackButtonListener
import ru.radiationx.anilibria.ui.common.IntentHandler
import ru.radiationx.anilibria.ui.common.ScreenMessagesObserver
import ru.radiationx.quill.get
import ru.radiationx.quill.inject
import ru.radiationx.quill.installModules
import ru.radiationx.shared.ktx.android.getExtraNotNull
import ru.radiationx.shared.ktx.android.putExtra
import ru.radiationx.shared.ktx.android.showWithLifecycle
import com.github.terrakok.cicerone.Navigator
import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.Router
import com.github.terrakok.cicerone.androidx.AppNavigator
import com.github.terrakok.cicerone.androidx.FragmentScreen

class TabFragment : Fragment(), BackButtonListener, IntentHandler, TopScroller, TabResetter {

    companion object {
        private const val ARG_ROOT_SCREEN = "LOCAL_ROOT_SCREEN"

        fun newInstance(rootScreen: BaseFragmentScreen) = TabFragment().putExtra {
            putSerializable(ARG_ROOT_SCREEN, rootScreen)
        }
    }

    private val screenMessagesObserver by inject<ScreenMessagesObserver>()

    private val linkHandler by inject<ILinkHandler>()

    private val router by inject<Router>()

    private val navigatorHolder by inject<NavigatorHolder>()

    private val localScreen: BaseFragmentScreen by lazy {
        getExtraNotNull(ARG_ROOT_SCREEN)
    }

    private val navigationQueue = mutableListOf<Runnable>()

    override fun onCreate(savedInstanceState: Bundle?) {
        installModules(RouterModule(localScreen.screenKey), MessengerModule())
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(screenMessagesObserver)
        if (needsToInitialScreen()) {
            navigationQueue.add(Runnable {
                router.newRootScreen(localScreen)
            })
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_tab_root, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (needsToInitialScreen()) {
            updateNavQueue()
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

    override fun onBackPressed(): Boolean {
        val fragment = childFragmentManager.findFragmentById(R.id.fragments_container)

        val handledByChild = (fragment as? BackButtonListener?)?.onBackPressed() ?: false
        if (handledByChild) {
            return true
        }

        if (childFragmentManager.backStackEntryCount >= 1) {
            router.exit()
            return true
        }

        return false
    }

    override fun handle(url: String): Boolean {
        val linkHandler = linkHandler
        linkHandler.findScreen(url)?.let {
            navigationQueue.add(Runnable {
                linkHandler.handle(url, router)
            })
            updateNavQueue()
            return true
        }
        return false
    }

    override fun scrollToTop() {
        val fragment = childFragmentManager.findFragmentById(R.id.fragments_container)
        if (fragment is TopScroller) {
            fragment.scrollToTop()
        }
    }

    override fun resetTab() {
        val count = childFragmentManager.backStackEntryCount
        if (count == 0) {
            return
        }
        AlertDialog.Builder(requireContext())
            .setMessage("Закрыть все экраны во вкладке?")
            .setPositiveButton("Да") { _, _ ->
                router.backTo(localScreen)
            }
            .setNegativeButton("Нет") { _, _ ->
                // do nothing
            }
            .showWithLifecycle(viewLifecycleOwner)
    }

    private fun needsToInitialScreen(): Boolean {
        return childFragmentManager.findFragmentById(R.id.fragments_container) == null
    }

    private fun updateNavQueue() {
        navigationQueue.forEach {
            it.run()
        }
        navigationQueue.clear()
    }

    private val navigatorLocal: Navigator by lazy {
        object :
            AppNavigator(requireActivity(), R.id.fragments_container, childFragmentManager) {

            override fun setupFragmentTransaction(
                screen: FragmentScreen,
                fragmentTransaction: FragmentTransaction,
                currentFragment: Fragment?,
                nextFragment: Fragment
            ) {
                if (currentFragment !is SharedProvider || nextFragment !is SharedReceiver) {
                    return
                }
                currentFragment.exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
                currentFragment.reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
                nextFragment.enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
                nextFragment.returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
                if (currentFragment.sharedViewLocal != null) {
                    setupSharedTransition(currentFragment, nextFragment, fragmentTransaction)
                }
            }

            override fun activityBack() {
                get<Router>().exit()
            }
        }
    }

    private fun setupSharedTransition(
        sharedProvider: SharedProvider,
        sharedReceiver: SharedReceiver,
        fragmentTransaction: FragmentTransaction,
    ) {

        val nextFragment = sharedReceiver as Fragment

        nextFragment.sharedElementEnterTransition = MaterialContainerTransform().apply {
            interpolator = FastOutSlowInInterpolator()
            setPathMotion(ArcMotion())
            scrimColor = Color.TRANSPARENT
            drawingViewId = R.id.fragments_container
        }

        sharedProvider.getSharedView()?.let {
            sharedReceiver.setTransitionName(it.transitionName)
            fragmentTransaction.addSharedElement(it, it.transitionName)
        }
    }
}
