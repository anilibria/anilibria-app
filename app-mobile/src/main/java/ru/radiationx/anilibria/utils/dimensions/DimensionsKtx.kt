package ru.radiationx.anilibria.utils.dimensions

import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.utils.view.attachedCoroutineScope
import ru.radiationx.quill.get

fun ViewHolder.applyDimensions(block: (Dimensions) -> Unit) {
    itemView.applyDimensions(block)
}

fun View.applyDimensions(block: (Dimensions) -> Unit) {
    val provider = get<DimensionsProvider>()
    provider.observe()
        .onEach { block.invoke(it) }
        .launchIn(attachedCoroutineScope)
}

fun View.getPaddings(): Offsets {
    return Offsets(paddingLeft, paddingTop, paddingRight, paddingBottom)
}

fun View.getMargins(): Offsets {
    return Offsets(marginLeft, marginTop, marginRight, marginBottom)
}

fun View.dimensionsApplier(): Lazy<DimensionsApplier> = lazy {
    DimensionsApplier(this)
}

fun ViewHolder.dimensionsApplier(): Lazy<DimensionsApplier> = lazy {
    DimensionsApplier(itemView)
}

class DimensionsApplier(private val view: View) {

    private val initialPaddings by lazy(LazyThreadSafetyMode.NONE) { view.getPaddings() }

    private val initialMargins by lazy(LazyThreadSafetyMode.NONE) { view.getMargins() }

    fun applyPaddings(vararg sides: Side) {
        view.applyDimensions { dimensions ->
            val offsets = computeOffset(initialPaddings, dimensions, *sides)
            view.updatePadding(offsets.left, offsets.top, offsets.right, offsets.bottom)
        }
    }

    fun applyMargins(vararg sides: Side) {
        view.applyDimensions { dimensions ->
            val offsets = computeOffset(initialMargins, dimensions, *sides)
            view.updateLayoutParams<MarginLayoutParams> {
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