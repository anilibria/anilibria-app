package ru.radiationx.anilibria.ui.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.Screens
import ru.radiationx.anilibria.ui.common.BackButtonListener
import ru.radiationx.anilibria.ui.common.RouterProvider
import ru.radiationx.anilibria.ui.fragments.articles.ArticlesFragment
import ru.radiationx.anilibria.ui.fragments.blogs.BlogsFragment
import ru.radiationx.anilibria.ui.fragments.other.OtherFragment
import ru.radiationx.anilibria.ui.fragments.release.ReleaseFragment
import ru.radiationx.anilibria.ui.fragments.releases.ReleasesFragment
import ru.radiationx.anilibria.ui.fragments.search.SearchFragment
import ru.radiationx.anilibria.ui.fragments.videos.VideosFragment


import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.Router
import ru.terrakok.cicerone.android.SupportFragmentNavigator

class TabFragment : Fragment(), RouterProvider, BackButtonListener {

    override lateinit var router: Router

    private var ciceroneHolder = App.navigation.local
    private lateinit var localScreen: String
    private lateinit var cicerone: Cicerone<Router>
    private var navigator: Navigator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            cicerone.router.newRootScreen(localScreen, 0)
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

                override fun createFragment(screenKey: String?, data: Any?): Fragment? {
                    return when (screenKey) {
                        Screens.MAIN_RELEASES -> ReleasesFragment()
                        Screens.MAIN_ARTICLES -> ArticlesFragment()
                        Screens.MAIN_VIDEOS -> VideosFragment()
                        Screens.MAIN_BLOGS -> BlogsFragment()
                        Screens.MAIN_OTHER -> OtherFragment()
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
}
