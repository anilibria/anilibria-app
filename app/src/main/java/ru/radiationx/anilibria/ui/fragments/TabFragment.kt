package ru.radiationx.anilibria.ui.fragments

import android.content.Context
import android.os.Bundle
import android.support.transition.*
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.transition.ChangeBounds
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.Screens
import ru.radiationx.anilibria.ui.common.BackButtonListener
import ru.radiationx.anilibria.ui.common.RouterProvider
import ru.radiationx.anilibria.ui.fragments.article.ArticleFragment
import ru.radiationx.anilibria.ui.fragments.articles.ArticlesFragment
import ru.radiationx.anilibria.ui.fragments.blogs.BlogsFragment
import ru.radiationx.anilibria.ui.fragments.release.ReleaseFragment
import ru.radiationx.anilibria.ui.fragments.releases.ReleasesFragment
import ru.radiationx.anilibria.ui.fragments.search.SearchFragment
import ru.radiationx.anilibria.ui.fragments.videos.VideosFragment


import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.Router
import ru.terrakok.cicerone.android.SupportFragmentNavigator
import ru.terrakok.cicerone.commands.Command
import ru.terrakok.cicerone.commands.Forward

class TabFragment : Fragment(), RouterProvider, BackButtonListener {

    override lateinit var router: Router

    private var ciceroneHolder = App.navigation.local
    private lateinit var localScreen: String
    private lateinit var cicerone: Cicerone<Router>
    private var navigator: Navigator? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        arguments?.let {
            localScreen = it.getString(LOCAL_ROOT_SCREEN, null) ?: throw NullPointerException()
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

    companion object {
        private val LOCAL_ROOT_SCREEN = "LOCAL_ROOT_SCREEN"

        fun newInstance(name: String): TabFragment {
            val fragment = TabFragment()

            val arguments = Bundle()
            arguments.putString(LOCAL_ROOT_SCREEN, name)
            fragment.arguments = arguments

            return fragment
        }
    }

    private fun getNavigator(): Navigator {
        if (navigator == null) {
            navigator = object : SupportFragmentNavigator(childFragmentManager, R.id.fragments_container) {

                override fun setupFragmentTransactionAnimation(command: Command?, currentFragment: Fragment?, nextFragment: Fragment?, fragmentTransaction: FragmentTransaction) {
                    Log.e("SUKA", "tranim $command\n$currentFragment\n$nextFragment")
                    if (command is Forward
                            && currentFragment is ReleasesFragment
                            && nextFragment is ReleaseFragment) {
                        setupSharedElementForProfileToSelectPhoto(
                                currentFragment,
                                nextFragment,
                                fragmentTransaction
                        )
                    }
                }

                override fun createFragment(screenKey: String?, data: Any?): Fragment? {
                    return when (screenKey) {
                        Screens.MAIN_RELEASES -> ReleasesFragment()
                        Screens.MAIN_ARTICLES -> ArticlesFragment()
                        Screens.MAIN_VIDEOS -> VideosFragment()
                        Screens.MAIN_BLOGS -> BlogsFragment()
                        Screens.MAIN_OTHER -> ArticleFragment()
                        Screens.ARTICLE_DETAILS -> {
                            val fragment = ArticleFragment()
                            if (data is Bundle) {
                                fragment.arguments = data
                            }
                            fragment
                        }
                        Screens.RELEASE_DETAILS -> {
                            val fragment = ReleaseFragment()
                            if (data is Bundle) {
                                fragment.arguments = data
                            }
                            fragment
                        }
                        Screens.RELEASES_SEARCH -> {
                            val fragment = SearchFragment()
                            if (data is Bundle) {
                                fragment.arguments = data
                            }
                            fragment
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

    private val MOVE_DEFAULT_TIME: Long = 375
    private val FADE_DEFAULT_TIME: Long = 225

    private fun setupSharedElementForProfileToSelectPhoto(
            currentFragment: ReleasesFragment,
            nextFragment: ReleaseFragment,
            fragmentTransaction: FragmentTransaction) {


        val exitFade = Fade()
        exitFade.duration = FADE_DEFAULT_TIME

        val enterFade = Fade()
        //enterFade.startDelay = MOVE_DEFAULT_TIME
        enterFade.duration = FADE_DEFAULT_TIME



        val enterTransitionSet = TransitionSet()
        enterTransitionSet.addTransition(TransitionInflater.from(context).inflateTransition(android.R.transition.move))
        enterTransitionSet.interpolator = FastOutSlowInInterpolator()
        enterTransitionSet.duration = MOVE_DEFAULT_TIME
        //enterTransitionSet.startDelay = FADE_DEFAULT_TIME
        nextFragment.sharedElementEnterTransition = enterTransitionSet

        currentFragment.exitTransition = enterFade
        nextFragment.enterTransition = enterFade

        enterTransitionSet.addListener(object : Transition.TransitionListener {
            override fun onTransitionEnd(transition: Transition) {
                Log.e("SUKA", "TRANSITION onTransitionEnd")
                if (nextFragment.enterTransition == enterFade) {
                    Log.e("SUKA", "TRANSITION SET EXIT")
                    nextFragment.enterTransition = exitFade
                    //currentFragment.exitTransition = enterFade
                } else {
                    Log.e("SUKA", "TRANSITION SET ENTER")
                    nextFragment.enterTransition = enterFade
                    //currentFragment.exitTransition = exitFade
                }
            }

            override fun onTransitionResume(transition: Transition) {
                Log.e("SUKA", "TRANSITION onTransitionResume")
            }

            override fun onTransitionPause(transition: Transition) {
                Log.e("SUKA", "TRANSITION onTransitionPause")
            }

            override fun onTransitionCancel(transition: Transition) {
                Log.e("SUKA", "TRANSITION onTransitionCancel")
            }

            override fun onTransitionStart(transition: Transition) {
                Log.e("SUKA", "TRANSITION onTransitionStart")
            }

        })


        /*val changeBounds = ChangeBounds()
        nextFragment.setSharedElementEnterTransition(changeBounds)
        nextFragment.setSharedElementReturnTransition(changeBounds)
        currentFragment.setSharedElementEnterTransition(changeBounds)
        currentFragment.setSharedElementReturnTransition(changeBounds)*/

        val view = currentFragment.getSharedView()
        nextFragment.transactioName = view.transitionName
        Log.e("SUKA", "view trans " + view)
        Log.e("SUKA", "view trans " + view.transitionName)
        fragmentTransaction.addSharedElement(view, view.transitionName)

        //nextFragment.setAnimationDestinationId(view.getTag() as Int)
    }
}
