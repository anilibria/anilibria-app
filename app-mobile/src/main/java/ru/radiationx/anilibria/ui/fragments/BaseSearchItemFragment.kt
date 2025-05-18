package ru.radiationx.anilibria.ui.fragments

import android.view.View
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.FragmentMainBaseSearchItemBinding

/* Created by radiationx on 18.11.17. */

abstract class BaseSearchItemFragment<ContentVB : ViewBinding>(
    @LayoutRes private val contentLayoutId: Int
) : BaseLayoutFragment<FragmentMainBaseSearchItemBinding, ContentVB>(
    baseLayoutId = R.layout.fragment_main_base_search_item,
    contentLayoutId = contentLayoutId
) {

    override fun onCreateBaseBinding(view: View): FragmentMainBaseSearchItemBinding {
        return FragmentMainBaseSearchItemBinding.bind(view)
    }
}
