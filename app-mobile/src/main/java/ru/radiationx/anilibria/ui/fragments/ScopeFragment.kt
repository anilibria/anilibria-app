package ru.radiationx.anilibria.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.ui.common.BackButtonListener
import ru.radiationx.anilibria.utils.DimensionHelper
import ru.radiationx.anilibria.utils.DimensionsProvider
import ru.radiationx.quill.inject

abstract class ScopeFragment(
    @LayoutRes contentLayoutId: Int
) : Fragment(contentLayoutId), BackButtonListener {

    private val dimensionsProvider by inject<DimensionsProvider>()

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dimensionsProvider
            .observe()
            .onEach { dimension ->
                interceptDimens(dimension)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    @CallSuper
    protected open fun interceptDimens(dimensions: DimensionHelper.Dimensions) {
        updateDimens(dimensions)
    }

    @CallSuper
    protected open fun updateDimens(dimensions: DimensionHelper.Dimensions) {
        // do nothing
    }
}
