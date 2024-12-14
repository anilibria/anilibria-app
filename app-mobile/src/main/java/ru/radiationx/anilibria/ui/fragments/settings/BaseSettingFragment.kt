package ru.radiationx.anilibria.ui.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.utils.DimensionsProvider
import ru.radiationx.quill.inject

/**
 * Created by radiationx on 24.09.17.
 */

open class BaseSettingFragment : PreferenceFragmentCompat() {

    private val dimensionsProvider by inject<DimensionsProvider>()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {}

    override fun onCreateRecyclerView(
        inflater: LayoutInflater,
        parent: ViewGroup,
        savedInstanceState: Bundle?,
    ): RecyclerView {
        val view = super.onCreateRecyclerView(inflater, parent, savedInstanceState)
        view.setPadding(0, 0, 0, 0)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDividerHeight(0)
        dimensionsProvider.observe().onEach {
            listView.updatePadding(bottom = it.navigationBar)
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }
}
