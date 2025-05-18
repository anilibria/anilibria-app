package ru.radiationx.anilibria.ui.fragments

import android.view.View
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.FragmentMainBaseSearchBinding

/* Created by radiationx on 18.11.17. */

abstract class BaseSearchFragment<ContentVB : ViewBinding>(
    @LayoutRes private val contentLayoutId: Int
) : BaseLayoutFragment<FragmentMainBaseSearchBinding, ContentVB>(
    baseLayoutId = R.layout.fragment_main_base_search,
    contentLayoutId = contentLayoutId
) {

    override fun onCreateBaseBinding(view: View): FragmentMainBaseSearchBinding {
        return FragmentMainBaseSearchBinding.bind(view)
    }
}
