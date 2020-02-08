package ru.radiationx.anilibria.ui.fragments.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by radiationx on 24.09.17.
 */

open class BaseSettingFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {}

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val rootView = view
        view?.let {
            it.findViewById<androidx.recyclerview.widget.RecyclerView>(androidx.preference.R.id.recycler_view)?.setPadding(0, 0, 0, 0)
        }
        setDividerHeight(0)
    }
}
