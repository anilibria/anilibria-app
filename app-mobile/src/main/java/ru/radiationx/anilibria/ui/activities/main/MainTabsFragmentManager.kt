package ru.radiationx.anilibria.ui.activities.main

import android.util.Log
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commitNow
import ru.radiationx.anilibria.navigation.BaseFragmentScreen
import ru.radiationx.anilibria.navigation.Screens

class MainTabsFragmentManager(
    private val activity: FragmentActivity,
    @IdRes private val containerId: Int
) {

    private val fragmentManager = activity.supportFragmentManager

    private var currentTabs = listOf<MainTab>()

    fun setState(tabs: List<MainTab>, selected: MainTab) {
        fragmentManager.commitNow {
            handleDeletedTabs(currentTabs, tabs)
            handleSelectedTab(selected)
            handleTabs(tabs, selected)
        }
        currentTabs = tabs.toList()
    }

    private fun FragmentTransaction.handleDeletedTabs(
        oldTabs: List<MainTab>,
        newTabs: List<MainTab>
    ) {
        val deletedTabs = oldTabs.toSet().subtract(newTabs.toSet())
        if (deletedTabs.isEmpty()) {
            return
        }
        deletedTabs.forEach { tab ->
            fragmentManager.findFragmentByTag(tab.key)?.also {
                remove(it)
            }
        }
    }

    private fun FragmentTransaction.handleSelectedTab(selected: MainTab) {
        val fragment = getOrCreateFragment(selected)
        if (!fragment.isAdded) {
            add(containerId, fragment, selected.key)
        }
        show(fragment)
    }

    private fun FragmentTransaction.handleTabs(tabs: List<MainTab>, selected: MainTab) {
        for (tab in tabs) {
            val fragment = fragmentManager.findFragmentByTag(tab.key) ?: continue
            if (tab == selected) {
                show(fragment)
            } else {
                hide(fragment)
            }
        }
    }

    private fun getOrCreateFragment(tab: MainTab): Fragment {
        return fragmentManager
            .findFragmentByTag(tab.key)
            ?: Screens.TabScreen(tab.getScreen()).createFragment(fragmentManager.fragmentFactory)
    }

    private fun MainTab.getScreen(): BaseFragmentScreen {
        return when (this) {
            MainTab.Feed -> Screens.MainFeed()
            MainTab.Favorites -> Screens.Favorites()
            MainTab.Catalog -> Screens.Catalog()
            MainTab.Collections -> Screens.Collections()
            MainTab.YouTube -> Screens.MainYouTube()
            MainTab.Other -> Screens.MainOther()
        }
    }


}