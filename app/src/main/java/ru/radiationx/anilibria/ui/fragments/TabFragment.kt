package ru.radiationx.anilibria.ui.fragments


import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.transition.*
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.Screens
import ru.radiationx.anilibria.ui.common.BackButtonListener
import ru.radiationx.anilibria.ui.common.RouterProvider
import ru.radiationx.anilibria.ui.fragments.article.details.ArticleFragment
import ru.radiationx.anilibria.ui.fragments.article.list.ArticlesFragment
import ru.radiationx.anilibria.ui.fragments.article.list.BlogsFragment
import ru.radiationx.anilibria.ui.fragments.article.list.VideosFragment
import ru.radiationx.anilibria.ui.fragments.other.OtherFragment
import ru.radiationx.anilibria.ui.fragments.release.details.ReleaseFragment
import ru.radiationx.anilibria.ui.fragments.release.list.ReleasesFragment
import ru.radiationx.anilibria.ui.fragments.search.SearchFragment
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.Router
import ru.terrakok.cicerone.android.SupportFragmentNavigator
import ru.terrakok.cicerone.commands.Command
import ru.terrakok.cicerone.commands.Forward

class TabFragment : Fragment(), RouterProvider, BackButtonListener {

    companion object {
        val TRANSITION_MOVE_TIME: Long = 375
        val TRANSITION_OTHER_TIME: Long = 225

        private val LOCAL_ROOT_SCREEN = "LOCAL_ROOT_SCREEN"

        fun newInstance(name: String): TabFragment {
            val fragment = TabFragment()
            fragment.arguments = Bundle()
            fragment.arguments?.apply {
                putString(LOCAL_ROOT_SCREEN, name)
            }
            return fragment
        }
    }

    override lateinit var router: Router

    private lateinit var localScreen: String

    private var ciceroneHolder = App.navigation.local
    private lateinit var cicerone: Cicerone<Router>
    private var navigator: Navigator? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        arguments?.let {
            localScreen = it.getString(LOCAL_ROOT_SCREEN, null) ?: throw NullPointerException("localScreen is null")
        }
        cicerone = ciceroneHolder.getCicerone(localScreen)
        router = cicerone.router
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tab, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (childFragmentManager.findFragmentById(R.id.fragments_container) == null) {
            cicerone.router.replaceScreen(localScreen)
        }
    }

    override fun onResume() {
        super.onResume()
        cicerone.navigatorHolder.setNavigator(getNavigator())
    }

    override fun onPause() {
        cicerone.navigatorHolder.removeNavigator()
        super.onPause()
    }

    override fun onBackPressed(): Boolean {
        val fragment = childFragmentManager.findFragmentById(R.id.fragments_container)
        return if (fragment != null
                && fragment is BackButtonListener
                && (fragment as BackButtonListener).onBackPressed()) {
            true
        } else {
            (activity as RouterProvider).router.exit()
            true
        }
    }

    private fun getNavigator(): Navigator {
        if (navigator == null) {
            navigator = object : SupportFragmentNavigator(childFragmentManager, R.id.fragments_container) {

                override fun setupFragmentTransactionAnimation(
                        command: Command?,
                        currentFragment: Fragment?,
                        nextFragment: Fragment?,
                        fragmentTransaction: FragmentTransaction) {

                    if (command is Forward && currentFragment is SharedProvider && nextFragment is SharedReceiver) {
                        setupSharedTransition(currentFragment, nextFragment, fragmentTransaction)
                    }/* else {
                        currentFragment?.let {
                            it.enterTransition = null
                            it.exitTransition = null
                        }
                        nextFragment?.let {
                            it.enterTransition = null
                            it.exitTransition = null
                        }
                    }*/
                }

                override fun createFragment(screenKey: String?, data: Any?): Fragment? {
                    return when (screenKey) {
                        Screens.MAIN_RELEASES -> ReleasesFragment()
                        Screens.MAIN_ARTICLES -> ArticlesFragment()
                        Screens.MAIN_VIDEOS -> VideosFragment()
                        Screens.MAIN_BLOGS -> BlogsFragment()
                        Screens.MAIN_OTHER -> OtherFragment()
                        Screens.ARTICLE_DETAILS -> {
                            ArticleFragment().apply {
                                if (data is Bundle) arguments = data
                            }
                        }
                        Screens.RELEASE_DETAILS -> {
                            ReleaseFragment().apply {
                                if (data is Bundle) arguments = data
                            }
                        }
                        Screens.RELEASES_SEARCH -> {
                            SearchFragment().apply {
                                if (data is Bundle) arguments = data
                            }
                        }
                        else -> throw RuntimeException("Unknown screen key: " + screenKey)
                    }
                }

                override fun showSystemMessage(message: String?) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }

                override fun exit() {
                    (activity as RouterProvider).router.exit()
                }
            }
        }
        return navigator as Navigator
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
                sharedReceiver.setTransitionName(it.transitionName)
                fragmentTransaction.addSharedElement(it, it.transitionName)
            }
        }
    }
}
