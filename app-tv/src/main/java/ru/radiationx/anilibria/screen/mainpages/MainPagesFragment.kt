package ru.radiationx.anilibria.screen.mainpages

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.ImageViewCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.PageRow
import androidx.transition.Fade
import androidx.transition.TransitionManager
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.fragment.BaseBrowseFragment
import ru.radiationx.anilibria.extension.getColorFromAttr
import ru.radiationx.anilibria.extension.getCompatColor
import ru.radiationx.anilibria.extension.getCompatDrawable

class MainPagesFragment : BaseBrowseFragment() {

    private val menuPresenter by lazy { ListRowPresenter() }
    private val menuAdapter by lazy { ArrayObjectAdapter(menuPresenter) }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainFragmentRegistry.registerFragment(PageRow::class.java, MainPagesFragmentFactory())

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
                title = MainPagesFragmentFactory.ids[selectedPosition].let { MainPagesFragmentFactory.variant1[it] }
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
            ColorStateList.valueOf(titleView.context.getCompatColor(R.color.dark_contrast_icon))
        )

    }

    private fun showMenu() {

        adapter = menuAdapter

        MainPagesFragmentFactory.ids.forEach {
            menuAdapter.add(PageRow(HeaderItem(it, MainPagesFragmentFactory.variant1[it])))
        }
        //startEntranceTransition()
    }
}