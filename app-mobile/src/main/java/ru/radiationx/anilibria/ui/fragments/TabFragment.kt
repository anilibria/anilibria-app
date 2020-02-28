package ru.radiationx.anilibria.ui.fragments


import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.di.MessengerModule
import ru.radiationx.anilibria.di.RouterModule
import ru.radiationx.shared_app.getDependency
import ru.radiationx.shared_app.injectDependencies
import ru.radiationx.anilibria.navigation.BaseAppScreen
import ru.radiationx.anilibria.presentation.common.ILinkHandler
import ru.radiationx.anilibria.ui.common.BackButtonListener
import ru.radiationx.anilibria.ui.common.IntentHandler
import ru.radiationx.anilibria.ui.common.ScopeProvider
import ru.radiationx.anilibria.ui.common.ScreenMessagesObserver
import ru.radiationx.shared.ktx.android.putExtra
import ru.radiationx.shared_app.DI
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import ru.terrakok.cicerone.commands.Command
import ru.terrakok.cicerone.commands.Forward
import javax.inject.Inject

class TabFragment : Fragment(), ScopeProvider, BackButtonListener, IntentHandler {

    companion object {
        private const val TRANSITION_MOVE_TIME: Long = 375
        private const val TRANSITION_OTHER_TIME: Long = 225
        private const val ARG_ROOT_SCREEN = "LOCAL_ROOT_SCREEN"

        fun newInstance(rootScreen: BaseAppScreen) = TabFragment().putExtra {
            putSerializable(ARG_ROOT_SCREEN, rootScreen)
        }
    }

    @Inject
    lateinit var screenMessagesObserver: ScreenMessagesObserver

    @Inject
    lateinit var linkHandler: ILinkHandler

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var navigatorHolder: NavigatorHolder

    private val localScreen: BaseAppScreen by lazy {
        arguments?.let {
            (it.getSerializable(ARG_ROOT_SCREEN) as? BaseAppScreen?)
        } ?: throw NullPointerException("localScreen is null")
    }

    override val screenScope: String by lazy { localScreen.screenKey }

    private val navigationQueue = mutableListOf<Runnable>()

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies(arrayOf(RouterModule(screenScope), MessengerModule()), DI.DEFAULT_SCOPE, screenScope)
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(screenMessagesObserver)
        navigationQueue.add(Runnable {
            router.newRootScreen(localScreen)
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tab_root, container, false)
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
        navigatorHolder.setNavigator(navigatorLocal)
    }

    override fun onPause() {
        navigatorHolder.removeNavigator()
        super.onPause()
    }

    override fun onBackPressed(): Boolean {
        val fragment = childFragmentManager.findFragmentById(R.id.fragments_container)
        return (fragment != null
                && fragment is BackButtonListener
                && (fragment as BackButtonListener).onBackPressed())
    }

    override fun handle(url: String): Boolean {
        val linkHandler = linkHandler
        Log.e("lalala", "IntentHandler $localScreen try handle $url")
        linkHandler.findScreen(url)?.let {
            Log.e("lalala", "IntentHandler $localScreen handled to screen=$it")
            Log.e(
                "lalala",
                "handle state $isAdded, $isDetached, $isHidden, $isInLayout, $isMenuVisible, $isRemoving, $isResumed, $isStateSaved, $isVisible"
            )
            navigationQueue.add(Runnable {
                linkHandler.handle(url, router)
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
        object : SupportAppNavigator(activity, childFragmentManager, R.id.fragments_container) {

            override fun setupFragmentTransaction(
                command: Command?,
                currentFragment: Fragment?,
                nextFragment: Fragment?,
                fragmentTransaction: FragmentTransaction
            ) {

                Log.e(
                    "lalala",
                    "setupFragmentTransaction $currentFragment, $nextFragment ;;; $screenScope ;;; shv=${(currentFragment as? SharedProvider)?.sharedViewLocal}"
                )
                val newScope = (currentFragment as? BaseFragment?)?.screenScope ?: screenScope
                nextFragment?.putExtra {
                    putString(BaseFragment.ARG_SCREEN_SCOPE, newScope)
                }

                if (command is Forward && currentFragment is SharedProvider && nextFragment is SharedReceiver) {
                    if (currentFragment.sharedViewLocal == null) {
                        currentFragment.exitTransition = Fade().apply {
                            duration = TRANSITION_OTHER_TIME
                        }
                        //nextFragment.enterTransition = Slide()
                        nextFragment.returnTransition = Explode().apply {
                            duration = TRANSITION_MOVE_TIME
                        }
                        //nextFragment.exitTransition = Slide()
                    } else {
                        //nextFragment.returnTransition = null
                        setupSharedTransition(currentFragment, nextFragment, fragmentTransaction)
                    }
                } else if (command is Forward && currentFragment is SharedReceiver && nextFragment is SharedReceiver) {
                    nextFragment.returnTransition = Explode().apply {
                        duration = TRANSITION_MOVE_TIME
                    }
                } else {
                    currentFragment?.exitTransition = null
                    nextFragment?.enterTransition = null
                    nextFragment?.returnTransition = null
                    nextFragment?.sharedElementEnterTransition = null
                }
            }

            override fun activityBack() {
                getDependency(Router::class.java).exit()
            }
        }
    }

    private fun setupSharedTransition(
        sharedProvider: SharedProvider,
        sharedReceiver: SharedReceiver,
        fragmentTransaction: FragmentTransaction
    ) {

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
