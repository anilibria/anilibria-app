package ru.radiationx.anilibria.screen.main

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.os.Handler
import android.text.format.DateUtils
import android.util.DisplayMetrics
import android.view.View
import androidx.leanback.app.BackgroundManager
import androidx.leanback.widget.*
import androidx.palette.graphics.Palette
import com.google.android.material.animation.ArgbEvaluatorCompat
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import dev.rx.tvtest.cust.CustomListRowPresenter
import dev.rx.tvtest.cust.CustomListRowViewHolder
import ru.radiationx.anilibria.LinkCard
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.common.MockData
import ru.radiationx.anilibria.common.fragment.BaseRowsFragment
import ru.radiationx.anilibria.ui.presenter.CardPresenterSelector
import ru.radiationx.anilibria.ui.presenter.LibriaCardPresenter
import ru.radiationx.data.entity.app.feed.FeedItem
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.entity.app.youtube.YoutubeItem
import java.util.*
import javax.inject.Inject

class MainFragment : BaseRowsFragment() {
    companion object {
        private val TAG = "MainFragment"

        private val BACKGROUND_UPDATE_DELAY = 100
        private val GRID_ITEM_WIDTH = 200
        private val GRID_ITEM_HEIGHT = 200
        private val NUM_ROWS = 5
        private val NUM_COLS = 20
    }

    private val paletteForImage = mutableMapOf<String, Palette>()

    private val mHandler = Handler()
    private lateinit var mBackgroundManager: BackgroundManager
    private var mDefaultBackground: Drawable? = null
    private lateinit var mMetrics: DisplayMetrics
    private var mBackgroundTimer: Timer? = null
    private var mBackgroundUri: String? = null

    private lateinit var layerDrawable: LayerDrawable
    private lateinit var colorDrawable: ColorDrawable
    private lateinit var gradientDrawable: GradientDrawable


    private var primaryColorAnimator: ValueAnimator? = null
    private fun updateBackground(uri: String?) {
        uri ?: return
        val palette = paletteForImage[uri]
        if (palette == null) {
            ImageLoader.getInstance().loadImage(uri, object : SimpleImageLoadingListener() {
                override fun onLoadingComplete(imageUri: String, view: View?, loadedImage: Bitmap) {
                    super.onLoadingComplete(imageUri, view, loadedImage)
                    generatePalette(imageUri, loadedImage)
                }
            })
        } else {
            applyPalette(palette)
        }



        mBackgroundTimer?.cancel()
    }

    private fun generatePalette(uri: String, bitmap: Bitmap) {
        Palette.Builder(bitmap)
            .generate {
                it?.also { palette ->
                    paletteForImage[uri] = palette
                    applyPalette(palette)
                }
            }
    }

    private fun applyPalette(palette: Palette) {
        val default = Color.MAGENTA
        val lightMuted = palette.getLightMutedColor(default)
        val lightVibrant = palette.getLightVibrantColor(lightMuted)
        val vibrant = palette.getVibrantColor(lightVibrant)
        val newColor = palette.getMutedColor(default)
        val dark = palette.getDarkMutedColor(Color.BLACK)

        primaryColorAnimator?.cancel()


        val colorAnimation = ValueAnimator.ofObject(
            ArgbEvaluatorCompat(),
            colorDrawable.color,
            newColor
        )
        colorAnimation.setDuration(500)
        colorAnimation.addUpdateListener {
            colorDrawable.color = it.animatedValue as Int
        }
        colorAnimation.start()
        primaryColorAnimator = colorAnimation
    }


    private fun startBackgroundTimer() {
        mBackgroundTimer?.cancel()
        mBackgroundTimer = Timer()
        mBackgroundTimer?.schedule(UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY.toLong())
    }

    private inner class UpdateBackgroundTask : TimerTask() {

        override fun run() {
            mHandler.post { updateBackground(mBackgroundUri) }
        }
    }


    private val rowsPresenter by lazy { CustomListRowPresenter() }
    private val rowsAdapter by lazy { ArrayObjectAdapter(rowsPresenter) }

    @Inject
    lateinit var mockData: MockData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        colorDrawable = ColorDrawable(Color.BLACK)
        gradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.BL_TR,
            intArrayOf(
                Color.parseColor("#EE110000"),
                Color.parseColor("#77000000")
            )
        )
        layerDrawable = LayerDrawable(
            arrayOf(
                colorDrawable,
                gradientDrawable
            )
        )


        mBackgroundManager = BackgroundManager.getInstance(activity)
        if (!mBackgroundManager.isAttached) {
            mBackgroundManager.attach(requireActivity().window)
            mBackgroundManager.isAutoReleaseOnStop = false
        }


        rowsAdapter.clear()
        createRow1()
        createRow2()
        createRow3()
        createRow4()
        adapter = rowsAdapter
        mainFragmentAdapter.fragmentHost.notifyDataReady(mainFragmentAdapter)
        onItemViewSelectedListener = ItemViewSelectedListener()
    }

    override fun onResume() {
        super.onResume()
        mBackgroundManager.drawable = layerDrawable
    }

    override fun onPause() {
        super.onPause()
        mBackgroundManager.clearDrawable()

    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun createRow1() {
        val presenterSelector = CardPresenterSelector()
        val adapter = ArrayObjectAdapter(presenterSelector).apply {
            addAll(0, mockData.feed.shuffled().map { it.toCard() })
            add(LinkCard("Смотреть всю ленту"))
        }
        val headerItem = HeaderItem("Самое актуальное")

        rowsAdapter.add(ListRow(headerItem, adapter))
    }

    private fun createRow2() {
        val presenterSelector = CardPresenterSelector()
        val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val items = mockData.schedule.first { it.day == day }.items
        val adapter = ArrayObjectAdapter(presenterSelector).apply {
            addAll(0, items.shuffled().map { it.releaseItem.toCard() })
            add(LinkCard("Открыть расписание"))
        }
        val headerItem = HeaderItem("Ожидается сегодня")

        rowsAdapter.add(ListRow(headerItem, adapter))
    }

    private fun createRow3() {
        val presenterSelector = CardPresenterSelector()
        val adapter = ArrayObjectAdapter(presenterSelector).apply {
            addAll(0, mockData.releases.shuffled().map { it.toCard() })
            add(LinkCard("Открыть избранное"))
        }
        val headerItem = HeaderItem("Обновления в избранном")

        rowsAdapter.add(ListRow(headerItem, adapter))
    }

    private fun createRow4() {
        val presenterSelector = CardPresenterSelector()
        val adapter = ArrayObjectAdapter(presenterSelector).apply {
            addAll(0, mockData.youtube.shuffled().map { it.toCard() })
            add(LinkCard("Открыть ролики YouTube"))
        }
        val headerItem = HeaderItem("Обновления на YouTube")

        rowsAdapter.add(ListRow(headerItem, adapter))
    }

    @SuppressLint("DefaultLocale")
    private fun ReleaseItem.toCard() = LibriaCard(
        id,
        title.orEmpty(),
        "${seasons.firstOrNull()} год • ${genres.firstOrNull()
            ?.capitalize()} • Серии: ${series} • Обновлен ${Date(torrentUpdate * 1000L).relativeDate().decapitalize()}",
        poster.orEmpty(),
        LibriaCard.Type.RELEASE
    )

    private fun YoutubeItem.toCard() = LibriaCard(
        id,
        title.orEmpty(),
        "Вышел ${Date(timestamp * 1000L).relativeDate().decapitalize()}",
        image.orEmpty(),
        LibriaCard.Type.YOUTUBE
    )

    private fun FeedItem.toCard(): LibriaCard = when {
        release != null -> release!!.toCard()
        youtube != null -> youtube!!.toCard()
        else -> throw RuntimeException("WataFuq")
    }

    private fun Date.relativeDate() = DateUtils.getRelativeDateTimeString(
        requireContext(),
        time,
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.DAY_IN_MILLIS * 2,
        DateUtils.FORMAT_SHOW_TIME
    ).toString()

    private inner class ItemViewSelectedListener : OnItemViewSelectedListener {
        override fun onItemSelected(
            itemViewHolder: Presenter.ViewHolder?, item: Any?,
            rowViewHolder: RowPresenter.ViewHolder, row: Row
        ) {
            if (rowViewHolder is CustomListRowViewHolder) {
                when (item) {
                    is LibriaCard -> {
                        rowViewHolder.setDescription(item.title, item.description)
                        mBackgroundUri = item.image
                        startBackgroundTimer()
                    }
                    is LinkCard -> {
                        rowViewHolder.setDescription(item.title, "")
                    }
                }
            }

        }
    }

}