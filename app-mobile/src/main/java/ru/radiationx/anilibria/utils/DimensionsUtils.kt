package ru.radiationx.anilibria.utils

import androidx.core.view.*
import ru.radiationx.anilibria.databinding.ActivityMainBinding
import kotlin.math.max

fun ActivityMainBinding.initInsets(provider: DimensionsProvider) {
    ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
        val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())

        val containerInsetList = listOf(
            systemBarInsets.bottom,
            tabsRecycler.height,
            imeInsets.bottom
        )
        val containerInsetsBottom = containerInsetList.max()

        val dimensions = Dimensions(
            statusBar = systemBarInsets.top,
            navigationBar = max(systemBarInsets.bottom, imeInsets.bottom),
        )
        layoutActivityContainer.root.updatePadding(bottom = containerInsetsBottom)
        configuringContainer.updatePadding(
            top = systemBarInsets.top,
            bottom = systemBarInsets.bottom
        )
        tabsRecycler.updatePadding(bottom = systemBarInsets.bottom)
        provider.update(dimensions)
        insets
    }

    root.doOnAttach {
        it.requestApplyInsets()
    }

    tabsRecycler.doOnLayout {
        root.requestApplyInsets()
    }
}