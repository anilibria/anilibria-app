package ru.radiationx.anilibria.ui.fragments


import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.transition.*
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.Screens
import ru.radiationx.anilibria.extension.putExtra
import ru.radiationx.anilibria.ui.common.BackButtonListener
import ru.radiationx.anilibria.ui.common.IntentHandler
import ru.radiationx.anilibria.ui.common.RouterProvider
import ru.radiationx.anilibria.ui.navigation.AppNavigator
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.Navigator
import ru.radiationx.anilibria.ui.navigation.AppRouter
import ru.terrakok.cicerone.commands.Command
import ru.terrakok.cicerone.commands.Forward

class TabFragment : Fragment(), RouterProvider, BackButtonListener, IntentHandler {

    companion object {
        private const val TRANSITION_MOVE_TIME: Long = 375
        private const val TRANSITION_OTHER_TIME: Long = 225
        private const val ARG_ROOT_SCREEN = "LOCAL_ROOT_SCREEN"

        fun newInstance(rootScreen: Screens.AppScreen) = TabFragment().putExtra {
            putSerializable(TabFragment.ARG_ROOT_SCREEN, rootScreen)
        }
    }

    lateinit var localRouter: AppRouter

    override fun getRouter(): AppRouter = localRouter
    override fun getNavigator(): Navigator = navigatorLocal
    private lateinit var localScreen: Screens.AppScreen

    private var ciceroneHolder = App.navigation.local
    private lateinit var cicerone: Cicerone<AppRouter>
    private val navigationQueue = mutableListOf<Runnable>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigationQueue.add(Runnable {
            cicerone.router.newRootScreen(localScreen)
        })
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        arguments?.let {
            Log.d("kekeke", "${it.getSerializable(ARG_ROOT_SCREEN)}")
            localScreen = (it.getSerializable(ARG_ROOT_SCREEN) as? Screens.AppScreen?)
                    ?: throw NullPointerException("localScreen is null")
        }
        cicerone = ciceroneHolder.getCicerone(localScreen.screenKey)
        localRouter = cicerone.router
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e("lalala", "onViewCreated $localScreen $this")
        if (childFragmentManager.findFragmentById(R.id.fragments_container) == null) {
            updateNavQueue()
        }
    }

    override fun onResume() {
        super.onResume()
        cicerone.navigatorHolder.setNavigator(navigatorLocal)
    }

    override fun onPause() {
        cicerone.navigatorHolder.removeNavigator()
        super.onPause()
    }

    override fun onBackPressed(): Boolean {
        val fragment = childFragmentManager.findFragmentById(R.id.fragments_container)
        return (fragment != null
                && fragment is BackButtonListener
                && (fragment as BackButtonListener).onBackPressed())
    }

    override fun handle(url: String): Boolean {
        val linkHandler = App.injections.linkHandler
        Log.e("lalala", "IntentHandler $localScreen try handle $url")
        linkHandler.findScreen(url)?.let {
            Log.e("lalala", "IntentHandler $localScreen handled to screen=$it")
            Log.e("lalala", "handle state $isAdded, $isDetached, $isHidden, $isInLayout, $isMenuVisible, $isRemoving, $isResumed, $isStateSaved, $isVisible")
            navigationQueue.add(Runnable {
                linkHandler.handle(url, localRouter)
            })
            updateNavQueue()
            return true
        }
        return false
    }

    private fun updateNavQueue() {
        navigationQueue.forEach {
            it.run()
        }
        navigationQueue.clear()
    }


    private val navigatorLocal: Navigator by lazy {
        object : AppNavigator(activity, childFragmentManager, R.id.fragments_container) {

            override fun setupFragmentTransaction(
                    command: Command?,
                    currentFragment: Fragment?,
                    nextFragment: Fragment?,
                    fragmentTransaction: FragmentTransaction
            ) {
                if (command is Forward && currentFragment is SharedProvider && nextFragment is SharedReceiver) {
                    setupSharedTransition(currentFragment, nextFragment, fragmentTransaction)
                }
            }

            override fun activityBack() {
                (activity as RouterProvider).getRouter().exit()
            }
        }
    }

    private fun setupSharedTransition(
            sharedProvider: SharedProvider,
            sharedReceiver: SharedReceiver,
            fragmentTransaction: FragmentTransaction) {

        val currentFragment = sharedProvider as Fragment
        val nextFragment = sharedReceiver as Fragment

        val exitFade = Fade()
        exitFade.duration = TRANSITION_OTHER_TIME

        val enterFade = Fade()
        enterFade.duration = TRANSITION_OTHER_TIME

        nextFragment.enterTransition = enterFade

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            currentFragment.exitTransition = enterFade

            val enterTransitionSet = TransitionSet()
            enterTransitionSet.addTransition(TransitionInflater.from(context).inflateTransition(android.R.transition.move))
            enterTransitionSet.setPathMotion(ArcMotion())
            enterTransitionSet.interpolator = FastOutSlowInInterpolator()
            enterTransitionSet.duration = TRANSITION_MOVE_TIME
            //enterTransitionSet.startDelay = TRANSITION_OTHER_TIME
            nextFragment.sharedElementEnterTransition = enterTransitionSet

            enterTransitionSet.addListener(object : Transition.TransitionListener {
                override fun onTransitionEnd(transition: Transition) {
                    nextFragment.apply {
                        enterTransition = if (enterTransition == enterFade) exitFade else enterFade
                    }
                }

                override fun onTransitionResume(transition: Transition) {}
                override fun onTransitionPause(transition: Transition) {}
                override fun onTransitionCancel(transition: Transition) {}
                override fun onTransitionStart(transition: Transition) {}
            })
        } else {
            currentFragment.exitTransition = exitFade
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            sharedProvider.getSharedView()?.let {
                Log.e("lalala", "TABFRAGMENT $it\n${it.transitionName}")
                sharedReceiver.setTransitionName(it.transitionName)
                fragmentTransaction.addSharedElement(it, it.transitionName)
            }
        }
    }
}
