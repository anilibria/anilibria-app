package ru.radiationx.anilibria.ui.fragments.other

import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.fragments.BaseFragment

/**
 * Created by radiationx on 16.12.17.
 */
class OtherFragment : BaseFragment() {
    override val layoutRes: Int = R.layout.fragment_blank

    override fun onBackPressed(): Boolean {
        return false
    }
}