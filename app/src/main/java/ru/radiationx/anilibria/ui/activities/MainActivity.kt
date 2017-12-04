package ru.radiationx.anilibria.ui.activities;

import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import kotlinx.android.synthetic.main.activity_main.*

import ru.radiationx.anilibria.App;
import ru.radiationx.anilibria.R;
import ru.radiationx.anilibria.R.id.bottomTabs
import ru.radiationx.anilibria.Screens;
import ru.radiationx.anilibria.ui.fragments.release.ReleaseFragment;
import ru.radiationx.anilibria.ui.fragments.releases.ReleasesFragment;
import ru.radiationx.anilibria.ui.fragments.search.SearchFragment
import ru.terrakok.cicerone.android.SupportFragmentNavigator;
import ru.terrakok.cicerone.commands.BackTo;
import ru.terrakok.cicerone.commands.Command;

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomTabs.setOnNavigationItemSelectedListener { item ->
            title = item.title
            true
        }

        addMenuToBottom(R.string.fragment_title_releases, R.drawable.ic_releases)
                .setOnMenuItemClickListener {
                    navigator.applyCommand(BackTo(Screens.RELEASES_LIST))
                    false
                }
        addMenuToBottom(R.string.fragment_title_news, R.drawable.ic_news)
                .setOnMenuItemClickListener {
                    navigator.applyCommand(BackTo(Screens.RELEASES_LIST))
                    false
                }
        addMenuToBottom(R.string.fragment_title_videos, R.drawable.ic_videos)
                .setOnMenuItemClickListener {
                    navigator.applyCommand(BackTo(Screens.RELEASES_LIST))
                    false
                }
        addMenuToBottom(R.string.fragment_title_blogs, R.drawable.ic_blogs)
                .setOnMenuItemClickListener {
                    navigator.applyCommand(BackTo(Screens.RELEASES_LIST))
                    false
                }
        addMenuToBottom(R.string.fragment_title_other, R.drawable.ic_other)
                .setOnMenuItemClickListener {
                    navigator.applyCommand(BackTo(Screens.RELEASES_LIST))
                    false
                }

        bottomTabs.enableItemShiftingMode(false)
        bottomTabs.enableShiftingMode(false)
        bottomTabs.setTextVisibility(false)
        bottomTabs.enableAnimation(false)

        Log.e("SUKA", "" + supportFragmentManager.fragments.size)
        val fragments = supportFragmentManager.fragments
        for (fragment in fragments) {
            Log.e("SUKA", "Old fragments: " + fragment)
        }
        if (savedInstanceState == null) {
            App.get().router.newRootScreen(Screens.RELEASES_LIST)
            //App.get().router.newRootScreen(Screens.RELEASES_SEARCH)
        }
    }

    private fun addMenuToBottom(title: String, @DrawableRes iconId: Int): MenuItem {
        return bottomTabs.menu.add(title).setIcon(iconId)
    }

    private fun addMenuToBottom(@StringRes titleId: Int, @DrawableRes iconId: Int): MenuItem {
        return bottomTabs.menu.add(titleId).setIcon(iconId)
    }

    private fun getCurrentTab(): MenuItem {
        return bottomTabs.menu.getItem(bottomTabs.currentItem)
    }

    private val navigator = object : SupportFragmentNavigator(supportFragmentManager, R.id.fragments_container) {

        override fun applyCommand(command: Command?) {
            super.applyCommand(command)
            val item = getCurrentTab()
            title = item.title
            Log.e("SUKA", "Fragments size: " + supportFragmentManager.fragments.size)
            val fragments = supportFragmentManager.fragments
            for (fragment in fragments) {
                Log.e("SUKA", "Fragment: " + fragment)
            }
        }

        override fun createFragment(screenKey: String?, data: Any?): Fragment? {
            Log.e("SUKA", "Create fragment $screenKey : $data")
            return when (screenKey) {
                Screens.RELEASE_DETAILS -> {
                    val fragment = ReleaseFragment()
                    if (data is Bundle) {
                        fragment.arguments = data
                    }
                    fragment
                }
                Screens.RELEASES_LIST -> {
                    ReleasesFragment()
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
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
        }

        override fun exit() {
            finish()
        }
    }


    override fun onResume() {
        super.onResume()
        App.get().navigatorHolder.setNavigator(navigator)
    }

    override fun onPause() {
        super.onPause()
        App.get().navigatorHolder.removeNavigator()
    }
}
