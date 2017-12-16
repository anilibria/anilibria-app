package ru.radiationx.anilibria.ui.activities

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.Screens
import ru.radiationx.anilibria.ui.common.BackButtonListener
import ru.radiationx.anilibria.ui.common.RouterProvider
import ru.radiationx.anilibria.ui.fragments.TabFragment
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.Router
import ru.terrakok.cicerone.commands.Back
import ru.terrakok.cicerone.commands.Command
import ru.terrakok.cicerone.commands.Replace
import ru.terrakok.cicerone.commands.SystemMessage


class MainActivity : AppCompatActivity(), RouterProvider {

    override val router: Router = App.navigation.root.router
    private val navigationHolder = App.navigation.root.holder

    private val tabs = arrayOf(
            Tab(R.string.fragment_title_releases, R.drawable.ic_releases, Screens.RELEASES_LIST),
            Tab(R.string.fragment_title_news, R.drawable.ic_news, Screens.ARTICLES_LIST),
            Tab(R.string.fragment_title_videos, R.drawable.ic_videos, Screens.VIDEOS_LIST),
            Tab(R.string.fragment_title_blogs, R.drawable.ic_blogs, Screens.BLOGS_LIST),
            Tab(R.string.fragment_title_other, R.drawable.ic_other, Screens.OTHER_LIST)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initContainers()
        initBottomMenu()

        bottomTabs.enableItemShiftingMode(false)
        bottomTabs.enableShiftingMode(false)
        bottomTabs.setTextVisibility(false)
        bottomTabs.enableAnimation(false)

        if (savedInstanceState == null) {
            bottomTabs.currentItem = 0
            router.newRootScreen(Screens.RELEASES_LIST)
        }
    }

    override fun onResume() {
        super.onResume()
        navigationHolder.setNavigator(navigatorNew)
    }

    override fun onPause() {
        super.onPause()
        navigationHolder.removeNavigator()
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.root_container)
        if (fragment != null
                && fragment is BackButtonListener
                && (fragment as BackButtonListener).onBackPressed()) {
            return
        } else {
            router.exit()
        }
    }

    private fun initContainers() {
        val fm = supportFragmentManager
        val ta = fm.beginTransaction()
        tabs.forEach { tab ->
            var fragment: Fragment? = fm.findFragmentByTag(tab.screenKey)
            if (fragment == null) {
                fragment = TabFragment.newInstance(tab.screenKey)
                ta.add(R.id.root_container, fragment, tab.screenKey)
                        .detach(fragment)
            }
        }
        ta.commitNow()
    }

    private fun initBottomMenu() {
        tabs.forEachIndexed { _, tab ->
            bottomTabs.menu
                    .add(tab.title)
                    .setIcon(tab.icon)
                    .setOnMenuItemClickListener {
                        router.replaceScreen(tab.screenKey)
                        false
                    }
        }
    }

    private val navigatorNew = object : Navigator {
        override fun applyCommand(command: Command) {
            Log.e("SUKA", "ApplyCommand " + command)
            if (command is Back) {
                finish()
            } else if (command is SystemMessage) {
                Toast.makeText(this@MainActivity, command.message, Toast.LENGTH_SHORT).show()
            } else if (command is Replace) {
                Log.e("SUKA", "Replace " + command.screenKey)
                val fm = supportFragmentManager
                val ta = fm.beginTransaction()
                tabs.forEach {
                    val fragment = fm.findFragmentByTag(it.screenKey)
                    if (it.screenKey == command.screenKey) {
                        ta.attach(fragment)
                    } else {
                        ta.detach(fragment)
                    }
                }
                ta.commitNow()
            }
        }
    }

    class Tab(val title: Int, val icon: Int, val screenKey: String)
}
