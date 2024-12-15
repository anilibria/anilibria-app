package ru.radiationx.anilibria.utils.dimensions

import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.utils.view.attachedCoroutineScope
import ru.radiationx.quill.get

class DimensionsApplier(private val view: View) {

    private val initialPaddings by lazy(LazyThreadSafetyMode.NONE) { view.getPaddingOffsets() }

    private val initialMargins by lazy(LazyThreadSafetyMode.NONE) { view.getMarginOffsets() }

    fun applyPaddings(vararg sides: Side) {
        view.applyDimensions { dimensions ->
            val offsets = computeOffset(initialPaddings, dimensions, *sides)
            view.updatePadding(offsets.left, offsets.top, offsets.right, offsets.bottom)
        }
    }

    fun applyMargins(vararg sides: Side) {
        view.applyDimensions { dimensions ->
            val offsets = computeOffset(initialMargins, dimensions, *sides)
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = offsets.left
                topMargin = offsets.top
                rightMargin = offsets.right
                bottomMargin = offsets.bottom
            }
        }
    }

    private fun computeOffset(
        initial: Offsets,
        dimensions: Dimensions,
        vararg sides: Side
    ): Offsets {
        var leftOffset = initial.left
        var topOffset = initial.top
        var rightOffset = initial.right
        var bottomOffset = initial.bottom
        sides.forEach { side ->
            when (side) {
                Side.Left -> leftOffset += dimensions.left
                Side.Top -> topOffset += dimensions.top
                Side.Right -> rightOffset += dimensions.right
                Side.Bottom -> bottomOffset += dimensions.bottom
            }
        }
        return Offsets(leftOffset, topOffset, rightOffset, bottomOffset)
    }

    private fun View.applyDimensions(block: (Dimensions) -> Unit) {
        val provider = get<DimensionsProvider>()
        provider.observe()
            .onEach { block.invoke(it) }
            .launchIn(attachedCoroutineScope)
    }
}

enum class Side {
    Left, Top, Right, Bottom
}

data class Offsets(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
)