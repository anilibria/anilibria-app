package ru.radiationx.anilibria.utils

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach
import androidx.core.view.doOnLayout
import androidx.core.view.updatePadding
import ru.radiationx.anilibria.databinding.ActivityAuthBinding
import ru.radiationx.anilibria.databinding.ActivityMainBinding
import kotlin.math.max

fun ActivityMainBinding.initInsets(provider: DimensionsProvider) {
    ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
        val systemBarInsets = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())

        val containerInsetList = listOf(
            systemBarInsets.bottom,
            appFooter.height,
            imeInsets.bottom
        )
        val containerInsetsBottom = containerInsetList.max()

        val dimensions = Dimensions(
            left = systemBarInsets.left,
            top = systemBarInsets.top,
            right = systemBarInsets.right,
        )
        layoutActivityContainer.root.updatePadding(
            bottom = containerInsetsBottom
        )
        configuringContainer.updatePadding(
            left = systemBarInsets.left,
            top = systemBarInsets.top,
            right = systemBarInsets.right,
            bottom = systemBarInsets.bottom
        )
        appFooter.updatePadding(
            left = systemBarInsets.left,
            right = systemBarInsets.right,
            bottom = systemBarInsets.bottom
        )
        provider.update(dimensions)
        insets
    }

    root.doOnAttach {
        it.requestApplyInsets()
    }

    appFooter.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
        root.requestApplyInsets()
    }
    appFooter.doOnLayout {
        root.requestApplyInsets()
    }
}

fun ActivityAuthBinding.initInsets(provider: DimensionsProvider) {
    ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
        val contentInsets = insets.getInsets(
            WindowInsetsCompat.Type.systemBars()
                .or(WindowInsetsCompat.Type.displayCutout())
                .or(WindowInsetsCompat.Type.ime())
        )
        layoutActivityContainer.root.updatePadding(
            top = contentInsets.top,
            left = contentInsets.left,
            right = contentInsets.right,
            bottom = contentInsets.bottom
        )
        insets
    }

    root.doOnAttach {
        it.requestApplyInsets()
    }
}