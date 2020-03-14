package ru.radiationx.anilibria.screen.main

import androidx.fragment.app.Fragment
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.Row
import ru.radiationx.anilibria.screen.trash.TestFragment

class MainFragmentFactory : BrowseSupportFragment.FragmentFactory<Fragment>() {

    private val fragments = mutableMapOf<Any, Fragment>()

    override fun createFragment(rowObj: Any): Fragment {
        val row = rowObj as Row
        val fragment = fragments[row]
        if (fragment == null) {
            fragments[row] = EmptyFragment()
        }
        return fragments.getValue(row)
    }
}