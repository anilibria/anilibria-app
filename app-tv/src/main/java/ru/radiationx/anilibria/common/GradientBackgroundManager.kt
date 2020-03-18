package ru.radiationx.anilibria.common

import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import androidx.annotation.ColorInt
import androidx.fragment.app.FragmentActivity
import androidx.leanback.app.BackgroundManager
import androidx.palette.graphics.Palette
import com.google.android.material.animation.ArgbEvaluatorCompat
import com.jakewharton.rxrelay2.BehaviorRelay
import com.nostra13.universalimageloader.core.ImageLoader
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.extension.getCompatColor
import toothpick.InjectConstructor
import java.util.concurrent.TimeUnit

@InjectConstructor
class GradientBackgroundManager(
    private val activity: FragmentActivity
) {

    private val backgroundManager: BackgroundManager by lazy { BackgroundManager.getInstance(activity) }

    private val defaultColor = activity.getCompatColor(R.color.dark_colorAccent)

    private val colorDrawable = ColorDrawable(defaultColor)
    private val gradientDrawable = GradientDrawable(
        GradientDrawable.Orientation.BL_TR,
        intArrayOf(
            Color.parseColor("#EE110000"),
            Color.parseColor("#77000000")
        )
    )
    private val layerDrawable = LayerDrawable(arrayOf(colorDrawable, gradientDrawable))

    private var primaryColorAnimator: ValueAnimator? = null
    private var imageApplierDisposable = Disposables.disposed()
    private var colorApplierDisposable = Disposables.disposed()
    private val colorApplier = BehaviorRelay.create<Int>()
    private val colorEvaluator = ArgbEvaluatorCompat()
    private val urlColorMap = mutableMapOf<String, Int>()

    init {
        if (!backgroundManager.isAttached) {
            backgroundManager.attach(activity.window)
            backgroundManager.isAutoReleaseOnStop = false
        }

        colorApplierDisposable = colorApplier
            .debounce(250, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .distinctUntilChanged()
            .subscribe {
                instantApplyColor(it)
            }
    }

    fun clearGradient() {
        backgroundManager.clearDrawable()
    }

    fun applyDefault() {
        applyColor(defaultColor)
    }

    fun applyImage(url: String) {
        val color = urlColorMap[url]
        if (color != null) {
            applyColor(color)
            return
        }

        imageApplierDisposable.dispose()
        imageApplierDisposable = Single
            .fromCallable {
                ImageLoader.getInstance().loadImageSync(url)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
            .map {
                Palette.Builder(it).generate()
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                urlColorMap[url] = getColorFromPalette(it)
                applyPalette(it)
            }, {
                it.printStackTrace()
            })
    }

    fun applyPalette(palette: Palette) {
        applyColor(getColorFromPalette(palette))
    }

    fun applyColor(@ColorInt color: Int) {
        colorApplier.accept(color)
    }

    private fun getColorFromPalette(palette: Palette): Int {
        val lightMuted = palette.getLightMutedColor(defaultColor)
        val lightVibrant = palette.getLightVibrantColor(lightMuted)
        val vibrant = palette.getVibrantColor(lightVibrant)
        val muted = palette.getMutedColor(defaultColor)
        val dark = palette.getDarkMutedColor(Color.BLACK)
        return muted
    }

    private fun instantApplyColor(@ColorInt color: Int) {
        imageApplierDisposable.dispose()
        if (backgroundManager.drawable == null) {
            backgroundManager.drawable = layerDrawable
        }

        primaryColorAnimator?.cancel()
        primaryColorAnimator = ValueAnimator
            .ofObject(colorEvaluator, colorDrawable.color, color)
            .apply {
                duration = 500
                addUpdateListener {
                    colorDrawable.color = it.animatedValue as Int
                }
                start()
            }
    }


}