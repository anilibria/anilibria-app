package ru.radiationx.anilibria.ui.fragments.settings

import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * Created by radiationx on 24.09.17.
 */

open class BaseSettingFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {}

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val rootView = view
        view?.let {
            it.findViewById<RecyclerView>(android.support.v7.preference.R.id.list)?.setPadding(0, 0, 0, 0)
        }
        setDividerHeight(0)
    }
}
