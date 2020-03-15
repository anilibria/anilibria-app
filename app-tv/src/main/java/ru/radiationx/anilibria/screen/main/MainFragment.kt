package ru.radiationx.anilibria.screen.main

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.widget.ImageViewCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.PageRow
import androidx.transition.AutoTransition
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.fragment.BaseBrowseFragment
import ru.radiationx.anilibria.extension.getColorFromAttr
import ru.radiationx.anilibria.extension.getCompatColor
import ru.radiationx.anilibria.extension.getCompatDrawable

class MainFragment : BaseBrowseFragment() {

    private val menuPresenter by lazy { ListRowPresenter() }
    private val menuAdapter by lazy { ArrayObjectAdapter(menuPresenter) }

    val variant1 = listOf(
        "Главная",
        "Я смотрю",
        "Сериалы",
        "Фильмы",
        "Поиск",
        "YouTube",
        "Профиль"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainFragmentRegistry.registerFragment(PageRow::class.java, MainFragmentFactory())

        setupUi()
        showMenu()
    }

    private fun setupUi() {
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true
        setOnSearchClickedListener {
            Toast.makeText(activity, "Serch clckd", Toast.LENGTH_SHORT).show()
        }

        setBrowseTransitionListener(object : BrowseTransitionListener() {
            override fun onHeadersTransitionStart(withHeaders: Boolean) {
                super.onHeadersTransitionStart(withHeaders)
                Log.e("lalala", "onHeadersTransitionStart $withHeaders, $selectedPosition")
            }

            override fun onHeadersTransitionStop(withHeaders: Boolean) {
                super.onHeadersTransitionStop(withHeaders)

                TransitionManager.beginDelayedTransition(titleView as ViewGroup, Fade(Fade.IN).apply {
                    interpolator = FastOutSlowInInterpolator()
                })
                badgeDrawable = if (withHeaders) {
                    requireContext().getCompatDrawable(R.drawable.ic_anilibria_splash)
                } else {
                    null
                }
                title = variant1[selectedPosition]
                Log.e("lalala", "onHeadersTransitionStop $withHeaders")
            }
        })

        setOnItemViewSelectedListener { itemViewHolder, item, rowViewHolder, row ->
            Log.e("lalala", "setOnItemViewSelectedListener $itemViewHolder, $item, $rowViewHolder, $row")
        }
        //brandColor = resources.getColor(R.color.dark_colorAccent)
        title = "AniLibria"
        badgeDrawable = if (isShowingHeaders) {
            requireContext().getCompatDrawable(R.drawable.ic_anilibria_splash)
        } else {
            null
        }
        //prepareEntranceTransition()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        ImageViewCompat.setImageTintList(
            titleView.findViewById(R.id.title_badge),
            ColorStateList.valueOf(titleView.context.getColorFromAttr(android.R.attr.colorAccent))
        )

    }

    private fun showMenu() {

        adapter = menuAdapter

        variant1.forEachIndexed { index, s ->
            menuAdapter.add(PageRow(HeaderItem(index.toLong(), s)))
        }
        //startEntranceTransition()
    }
}