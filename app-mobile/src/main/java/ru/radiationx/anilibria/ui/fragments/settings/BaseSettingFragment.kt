package ru.radiationx.anilibria.ui.fragments.settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroupAdapter
import androidx.preference.PreferenceScreen
import androidx.preference.PreferenceViewHolder
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.utils.dimensions.DimensionsApplier
import ru.radiationx.anilibria.utils.dimensions.DimensionsProvider
import ru.radiationx.anilibria.utils.dimensions.Side
import ru.radiationx.quill.inject
import java.util.WeakHashMap

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

    @SuppressLint("RestrictedApi")
    override fun onCreateAdapter(preferenceScreen: PreferenceScreen): RecyclerView.Adapter<*> {
        return object : PreferenceGroupAdapter(preferenceScreen) {
            private val appliers = WeakHashMap<PreferenceViewHolder, DimensionsApplier>()

            override fun onBindViewHolder(holder: PreferenceViewHolder, position: Int) {
                val applier = appliers.getOrPut(holder) {
                    DimensionsApplier(holder.itemView)
                }
                applier.applyPaddings(Side.Left, Side.Right)
                super.onBindViewHolder(holder, position)
            }

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDividerHeight(0)
        dimensionsProvider.observe().onEach {
            listView.updatePadding(bottom = it.bottom)
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }
}
