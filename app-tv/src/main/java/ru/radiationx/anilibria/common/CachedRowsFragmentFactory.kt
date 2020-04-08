package ru.radiationx.anilibria.common

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.app.RowsSupportFragment
import androidx.leanback.widget.Row
import ru.radiationx.anilibria.screen.main.MainFragment
import ru.radiationx.anilibria.screen.mainpages.EmptyFragment
import ru.radiationx.anilibria.screen.profile.ProfileFragment
import ru.radiationx.anilibria.screen.watching.WatchingFragment
import ru.radiationx.anilibria.screen.youtube.YoutubeFragment
import ru.radiationx.shared_app.di.putScopeArgument

open class CachedRowsFragmentFactory : BrowseSupportFragment.FragmentFactory<Fragment>() {

    private val fragments = mutableMapOf<Long, Fragment>()

    var behavior = Behavior.CACHE

    override fun createFragment(rowObj: Any): Fragment {
        val row = rowObj as Row
        return if (behavior == Behavior.CACHE) {
            getFromMap(row)
        } else {
            getFragmentByRow(row)
        }
    }

    private fun getFromMap(row: Row): Fragment {
        val fragment = fragments[row.id]
        if (fragment == null) {
            fragments[row.id] = getFragmentByRow(row)
        }
        return fragments.getValue(row.id)
    }

    open fun getFragmentByRow(row: Row): Fragment = when (row.id) {
        else -> RowsSupportFragment()
    }

    enum class Behavior {
        DEFAULT, CACHE
    }
}