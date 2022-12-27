package ru.radiationx.anilibria.common

import android.animation.ValueAnimator
import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt
import kotlin.math.*


class LinearGradientDrawable(
    private var angle: Float = 0f,
    private var colorValues: IntArray? = null,
    private var colorPositions: FloatArray? = null,
    private var withCoercing: Boolean = false
) : Drawable() {

    companion object {
        private const val DEBUG = false
        private val red by lazy {
            Paint().apply {
                color = Color.RED
                isAntiAlias = true
                style = Paint.Style.STROKE
                textSize = 48f
            }
        }
    }

    private var floatBounds = RectF()
    private var radius: Float = 0f
    private var centerPoint: PointF = PointF()
    private var startPoint: PointF = PointF()
    private val gradientPaint = Paint()


    val blue = Paint().apply {
        color = Color.BLUE
        isAntiAlias = true
    }

    init {
        val valueAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 20000
            interpolator = LinearInterpolator()
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            startDelay = 1000
            addUpdateListener {
                val angle = it.animatedValue as Float
                this@LinearGradientDrawable.angle = angle
                invalidateSelf()
            }
        }
        //valueAnimator.start()
    }

    fun setColors(@ColorInt colorStart: Int, @ColorInt colorEnd: Int) {
        colorValues = intArrayOf(colorStart, colorEnd)
        colorPositions = floatArrayOf(0f, 1f)
        invalidateSelf()
    }

    fun setMultipleColors(colors: IntArray?) {
        val positions = colors?.mapIndexed { index: Int, _: Int ->
            (index + 1) / (colors.size.toFloat())
        }
        colorValues = colors?.copyOf()
        colorPositions = positions?.toFloatArray()
        invalidateSelf()
    }

    fun setMultipleColors(colors: IntArray?, positions: FloatArray? = null) {
        colorValues = colors?.copyOf()
        colorPositions = positions?.copyOf()
        invalidateSelf()
    }

    fun setAngle(angle: Float) {
        this.angle = angle
        invalidateSelf()
    }

    fun setCoercing(coercing: Boolean) {
        withCoercing = coercing
        invalidateSelf()
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)

        floatBounds = RectF(bounds)
        val radius = sqrt(floatBounds.right.pow(2f) + floatBounds.bottom.pow(2f)) / 2f

        centerPoint = PointF(floatBounds.centerX(), floatBounds.centerY())
        startPoint = PointF(floatBounds.centerX(), radius + floatBounds.centerY())
        //val point = PointF(bounds.centerX(), bounds.bottom)
    }

    override fun draw(canvas: Canvas) {
        val colors = colorValues ?: return
        val piece = 1f / (colors.size - 1)
        val positions = colorPositions ?: colors.mapIndexed { index: Int, _: Int ->
            piece * (index)
        }.toFloatArray()

        if (colorPositions == null) {
            colorPositions = positions
        }

        var rotatedPoint = startPoint.rotate(centerPoint, angle)
        var invertedPoint = rotatedPoint.scale(centerPoint, -1f)

        if (withCoercing) {
            rotatedPoint = rotatedPoint.coerceInBounds()
            invertedPoint = invertedPoint.coerceInBounds()
        }

        gradientPaint.shader = createShader(invertedPoint, rotatedPoint, colors, positions)

        canvas.drawPaint(gradientPaint)

        if (DEBUG) {
            canvas.drawLine(invertedPoint.x, invertedPoint.y, rotatedPoint.x, rotatedPoint.y, blue)
            canvas.drawText(
                "${
                    angle.toInt().toString()
                }, $radius, ${bounds.right}, ${rotatedPoint}", 100f, 100f, red
            )
        }
    }

    private fun createShader(
        start: PointF,
        end: PointF,
        colors: IntArray,
        positions: FloatArray
    ): Shader = LinearGradient(
        start.x, start.y,
        end.x, end.y,
        colors,
        positions,
        Shader.TileMode.CLAMP
    )

    fun PointF.rotate(center: PointF, rotationAngle: Float): PointF {
        val translatedAngle = 360 - rotationAngle
        val radianAngle = translatedAngle * Math.PI / 180
        val deltaX = x - center.x
        val deltaY = y - center.y
        val rotatedDeltaY = (deltaY * cos(radianAngle) - deltaX * sin(radianAngle)).toFloat()
        val rotatedDeltaX = (deltaY * sin(radianAngle) + deltaX * cos(radianAngle)).toFloat()
        return PointF(rotatedDeltaX + center.x, rotatedDeltaY + center.y)
    }

    private fun PointF.scale(center: PointF, scale: Float): PointF {
        val centerDeltaX = x - center.x
        val centerDeltaY = y - center.y
        val scaledDeltaX = abs(abs(centerDeltaX) - abs(centerDeltaX) * scale)
        val scaledDeltaY = abs(abs(centerDeltaY) - abs(centerDeltaY) * scale)
        val scaledTranslationX: Float
        val scaledTranslationY: Float
        scaledTranslationX = if (centerDeltaX < 0 && scale > 1 || centerDeltaX > 0 && scale < 1) {
            -scaledDeltaX
        } else {
            scaledDeltaX
        }
        scaledTranslationY = if (centerDeltaY < 0 && scale > 1 || centerDeltaY > 0 && scale < 1) {
            -scaledDeltaY
        } else {
            scaledDeltaY
        }
        return PointF(x + scaledTranslationX, y + scaledTranslationY)
    }

    private fun PointF.coerceInBounds(): PointF = PointF(
        x.coerceIn(floatBounds.left, floatBounds.right),
        y.coerceIn(floatBounds.top, floatBounds.bottom)
    )

    override fun setAlpha(alpha: Int) {}

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun setColorFilter(colorFilter: ColorFilter?) {}
}