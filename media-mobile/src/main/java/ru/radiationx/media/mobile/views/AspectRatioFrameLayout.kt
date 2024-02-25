package ru.radiationx.media.mobile.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import kotlin.math.abs

enum class ResizeMode {
    FIT,
    FIXED_WIDTH,
    FIXED_HEIGHT,
    FILL,
    ZOOM
}

internal class AspectRatioFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    companion object {

        private const val MAX_ASPECT_RATIO_DEFORMATION_FRACTION = 0.01f
    }

    private var _videoAspectRatio = 0f
    private var _resizeMode = ResizeMode.FIT

    fun setAspectRatio(ratio: Float) {
        if (_videoAspectRatio != ratio) {
            _videoAspectRatio = ratio
            requestLayout()
        }
    }

    fun getResizeMode(): ResizeMode {
        return _resizeMode
    }

    fun setResizeMode(resizeMode: ResizeMode) {
        if (_resizeMode != resizeMode) return
        _resizeMode = resizeMode
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (_videoAspectRatio <= 0) {
            // Aspect ratio not set.
            return
        }
        var width = measuredWidth
        var height = measuredHeight
        val viewAspectRatio = width.toFloat() / height
        val aspectDeformation = _videoAspectRatio / viewAspectRatio - 1
        if (abs(aspectDeformation) <= MAX_ASPECT_RATIO_DEFORMATION_FRACTION) {
            return
        }
        when (_resizeMode) {
            ResizeMode.FIXED_WIDTH -> {
                height = (width / _videoAspectRatio).toInt()
            }

            ResizeMode.FIXED_HEIGHT -> {
                width = (height * _videoAspectRatio).toInt()
            }

            ResizeMode.ZOOM -> {
                if (aspectDeformation > 0) {
                    width = (height * _videoAspectRatio).toInt()
                } else {
                    height = (width / _videoAspectRatio).toInt()
                }
            }

            ResizeMode.FIT -> {
                if (aspectDeformation > 0) {
                    height = (width / _videoAspectRatio).toInt()
                } else {
                    width = (height * _videoAspectRatio).toInt()
                }
            }

            ResizeMode.FILL -> {}
        }
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        )
    }


}