package ru.radiationx.anilibria.screen.trash

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import androidx.core.graphics.ColorUtils
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ClassPresenterSelector
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import dev.rx.tvtest.cust.CustomListRowPresenter
import dev.rx.tvtest.cust.CustomListRowViewHolder
import ru.radiationx.anilibria.LinkCard
import ru.radiationx.anilibria.common.*
import ru.radiationx.anilibria.common.fragment.scoped.ScopedRowsFragment
import ru.radiationx.anilibria.extension.applyCard
import ru.radiationx.anilibria.screen.GridScreen
import ru.radiationx.anilibria.screen.main.toCard
import ru.radiationx.anilibria.ui.presenter.CardPresenterSelector
import ru.radiationx.anilibria.ui.presenter.ReleaseDetailsPresenter
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.entity.app.schedule.ScheduleDay
import ru.terrakok.cicerone.Router
import java.util.*
import javax.inject.Inject

class TestDetailFragment : ScopedRowsFragment() {

    private val instantLoading = true
    private val rowsPresenter by lazy { ClassPresenterSelector() }
    private val rowsAdapter by lazy { ArrayObjectAdapter(rowsPresenter) }
    private val releaseItem by lazy {
        mockData.releases.firstOrNull { arguments?.getInt("id") == it.id }
            ?: mockData.feed.filter { it.release != null }.map { it.release!! }.firstOrNull { arguments?.getInt("id") == it.id }
    }
    private val details by lazy { releaseItem!!.toDetail(requireContext()) }

    @Inject
    lateinit var mockData: MockData

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var backgroundManager: GradientBackgroundManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.e("kekeke", "$this oncreate $savedInstanceState")

        adapter = rowsAdapter
        rowsPresenter.addClassPresenter(ListRow::class.java, CustomListRowPresenter())
        rowsPresenter.addClassPresenter(LibriaDetailsRow::class.java, ReleaseDetailsPresenter())

        setOnItemViewSelectedListener { itemViewHolder, item, rowViewHolder, row ->
            Log.e("kekeke", "select $item, $row")
            if (row is ListRow) {
                backgroundManager.applyCard(item)
            } else {
                applyImage()
            }
            if (rowViewHolder is CustomListRowViewHolder) {
                when (item) {
                    is LibriaCard -> {
                        rowViewHolder.setDescription(item.title, item.description)
                    }
                    is LinkCard -> {
                        rowViewHolder.setDescription(item.title, "")
                    }
                    is LoadingCard -> {
                        rowViewHolder.setDescription(item.errorTitle, item.errorDescription)
                    }
                    else -> {
                        rowViewHolder.setDescription("", "")
                    }
                }
            }
        }

        setOnItemViewClickedListener { itemViewHolder, item, rowViewHolder, row ->
            if (rowViewHolder is CustomListRowViewHolder) {

                router.navigateTo(GridScreen())
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (rowsAdapter.size() == 0) {
            createDetailRow()
            createRow1()
            createRow2()
        }
    }

    private fun applyImage() {
        backgroundManager.applyImage(details.image, colorSelector = {
            val swatch = it.darkVibrantSwatch ?: it.vibrantSwatch
            val color = swatch?.rgb
            Log.e("kekeke", "apply detail $color")
            color
            null
        }) {
            val hslColor = FloatArray(3)
            ColorUtils.colorToHSL(it, hslColor)
            hslColor[1] = (hslColor[1] + 0.05f).coerceAtMost(1.0f)
            hslColor[2] = (hslColor[2] + 0.05f).coerceAtMost(1.0f)
            ColorUtils.HSLToColor(hslColor)
        }
    }


    private fun createDetailRow() {

        applyImage()
        rowsAdapter.add(LibriaDetailsRow(details))
    }

    private fun createRow1() {
        val presenterSelector = CardPresenterSelector()
        val adapter = ArrayObjectAdapter(presenterSelector).apply {
            addAll(0, mockData.releases.shuffled().take(4).map { it.toCard(requireContext()) })
        }
        val headerItem = HeaderItem("Связанные релизы")
        rowsAdapter.add(ListRow(headerItem, adapter))
    }

    private fun createRow2() {
        val presenterSelector = CardPresenterSelector()
        val adapter = ArrayObjectAdapter(presenterSelector).apply {
            addAll(0, mockData.releases.shuffled().map { it.toCard(requireContext()) } + LinkCard("Смотреть всю ленту"))
        }
        val headerItem = HeaderItem("Похожие релизы")
        rowsAdapter.add(ListRow(headerItem, adapter))
    }
}

fun ReleaseItem.toDetail(context: Context) = LibriaDetails(
    id,
    title.orEmpty(),
    titleEng.orEmpty(),
    listOf(
        genres.firstOrNull()?.capitalize()?.trim(),
        "${seasons.firstOrNull()} год",
        types.firstOrNull()?.trim(),
        "Серии: ${series?.trim()}"
    ).joinToString(" • "),
    Html.fromHtml(description.orEmpty()).toString().trim().trim('"').let { it + it + it },
    announce?.trim()?.trim('.')?.capitalize() ?: days.firstOrNull()?.toAnnounce2().orEmpty(),
    poster.orEmpty()
)

fun String.toAnnounce(): String {
    val calendarDay = ScheduleDay.toCalendarDay(this)
    val displayDay = Calendar.getInstance().let {
        it.set(Calendar.DAY_OF_WEEK, calendarDay)
        it.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
    }.orEmpty()
    val prefix = calendarDay.dayIterationPrefix()
    return "Новая серия $prefix $displayDay"
}

fun String.toAnnounce2(): String {
    val calendarDay = ScheduleDay.toCalendarDay(this)
    val displayDay = Calendar.getInstance().let {
        it.set(Calendar.DAY_OF_WEEK, calendarDay)
        it.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
    }.orEmpty()
    val prefix = calendarDay.dayIterationPrefix2()
    return "Серии выходят $prefix"
}

fun Int.dayIterationPrefix(): String = when (this) {
    Calendar.MONDAY,
    Calendar.TUESDAY,
    Calendar.THURSDAY -> "каждый"
    Calendar.WEDNESDAY,
    Calendar.FRIDAY,
    Calendar.SATURDAY -> "каждую"
    Calendar.SUNDAY -> "каждое"
    else -> throw Exception("Not found day by $this")
}

fun Int.dayIterationPrefix2(): String = when (this) {
    Calendar.MONDAY -> "в понедельник"
    Calendar.TUESDAY -> "во вторник"
    Calendar.WEDNESDAY -> "в среду"
    Calendar.THURSDAY -> "в четверг"
    Calendar.FRIDAY -> "в пятницу"
    Calendar.SATURDAY -> "в субботу"
    Calendar.SUNDAY -> "в воскресенье"
    else -> throw Exception("Not found day by $this")
}