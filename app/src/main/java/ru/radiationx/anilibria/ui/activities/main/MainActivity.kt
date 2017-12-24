package ru.radiationx.anilibria.ui.activities.main

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
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


class MainActivity : MvpAppCompatActivity(), MainView, RouterProvider {

    override val router: Router = App.navigation.root.router
    private val navigationHolder = App.navigation.root.holder

    private val tabs = arrayOf(
            Tab(R.string.fragment_title_releases, R.drawable.ic_releases, Screens.MAIN_RELEASES),
            Tab(R.string.fragment_title_news, R.drawable.ic_news, Screens.MAIN_ARTICLES),
            Tab(R.string.fragment_title_videos, R.drawable.ic_videos, Screens.MAIN_VIDEOS),
            Tab(R.string.fragment_title_blogs, R.drawable.ic_blogs, Screens.MAIN_BLOGS),
            Tab(R.string.fragment_title_other, R.drawable.ic_other, Screens.MAIN_OTHER)
    )

    @InjectPresenter
    lateinit var presenter: MainPresenter

    @ProvidePresenter
    fun provideMainPresenter(): MainPresenter {
        return MainPresenter(router)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val window = window
        val winParams = window.attributes
        winParams.flags = winParams.flags and WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS.inv()
        window.attributes = winParams
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        initContainers()
        initBottomTabs()

        if (savedInstanceState == null) {
            presenter.selectTab(Screens.MAIN_RELEASES)
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
            presenter.onBackPressed()
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

    private fun initBottomTabs() {
        tabs.forEachIndexed { index, tab ->
            bottomTabs.menu
                    .add(Menu.NONE, index + 1, Menu.NONE, tab.title)
                    .setIcon(tab.icon)
                    .setOnMenuItemClickListener {
                        presenter.selectTab(tab.screenKey)
                        false
                    }
        }

        bottomTabs.enableItemShiftingMode(false)
        bottomTabs.enableShiftingMode(false)
        bottomTabs.setTextVisibility(false)
        bottomTabs.enableAnimation(false)
    }

    override fun highlightTab(screenKey: String) {
        tabs.forEachIndexed { index, tab ->
            if (tab.screenKey == screenKey) {
                val menuItem = bottomTabs.menu.findItem(index + 1)
                //Так не вызывается событие onclick. Позволяет избежать рекурсии при выборе таба
                menuItem.isEnabled = false
                menuItem.isChecked = true
                menuItem.isEnabled = true
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

    class Tab(val title: Int,
              val icon: Int,
              val screenKey: String)
}
