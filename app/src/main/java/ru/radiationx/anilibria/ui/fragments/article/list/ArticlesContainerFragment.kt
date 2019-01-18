package ru.radiationx.anilibria.ui.fragments.article.list

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.fragment_article_container.*
import kotlinx.android.synthetic.main.fragment_main_base.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.extension.getColorFromAttr
import ru.radiationx.anilibria.ui.common.RouterProvider
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.SharedProvider
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.Router

/**
 * Created by radiationx on 25.02.18.
 */
class ArticlesContainerFragment : BaseFragment(), RouterProvider, SharedProvider {
    override var sharedViewLocal: View? = null

    override fun getSharedView(): View? {
        val position = viewPager.currentItem
        val fragment = pagerAdapter.getItem(position)
        val sharedView = (fragment as SharedProvider).getSharedView()
        return sharedView
    }

    private val pagerAdapter by lazy { CustomPagerAdapter() }

    private val spinnerAdapter by lazy {
        ArrayAdapter<String>(
                spinner.context,
                R.layout.item_view_spinner
        )
    }

    override fun getLayoutResource(): Int = R.layout.fragment_article_container

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spinner.apply {
            spinnerContainer.visibility = View.VISIBLE

            dropDownVerticalOffset = (8 * context.resources.displayMetrics.density).toInt()
            adapter = spinnerAdapter
            spinnerAdapter.setDropDownViewResource(R.layout.item_view_spinner_dropdown)

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    val fragment = pagerAdapter.getItem(viewPager.currentItem) as ArticlesBaseFragment
                    Log.e("S_DEF_LOG", "TEST onItemSelected $p2, vp.ci=${viewPager.currentItem}, fr=$fragment")
                    fragment.onSelectCategory(fragment.spinnerItems[p2].first)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
        }

        val tabLayout = TabLayout(appbarLayout.context)
        appbarLayout.addView(tabLayout)
        tabLayout.background = null
        tabLayout.setTabTextColors(
                tabLayout.context.getColorFromAttr(R.attr.textColoredButton),
                tabLayout.context.getColorFromAttr(R.attr.textColoredButton)
        )
        viewPager.adapter = pagerAdapter
        tabLayout.setupWithViewPager(viewPager)
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            var first: Boolean = true
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                if (first && positionOffset == 0f && positionOffsetPixels == 0) {
                    onPageSelected(viewPager.currentItem);
                    first = false;
                }
            }

            override fun onPageSelected(position: Int) {
                val fragment = pagerAdapter.getItem(position) as ArticlesBaseFragment
                val titles = fragment.spinnerItems.map { it.second }
                val cats = fragment.spinnerItems.map { it.first }
                val indexCurrent = cats.indexOfFirst { it == fragment.category }
                Log.e("lalala", "onPageSelected: ${titles.size}, ${cats.size}, index=$indexCurrent")
                setSpinnerItems(titles)
                spinner.setSelection(indexCurrent)
            }
        })
    }

    fun setSpinnerItems(items: List<String>) {
        spinnerAdapter.clear()
        spinnerAdapter.addAll(items)
        spinnerAdapter.notifyDataSetChanged()
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun getRouter(): Router = (parentFragment as RouterProvider).getRouter()
    override fun getNavigator(): Navigator = (parentFragment as RouterProvider).getNavigator()

    inner class CustomPagerAdapter : FragmentStatePagerAdapter(childFragmentManager) {
        private val fragments = mutableListOf<Fragment>()

        private val titles = listOf(
                getString(R.string.fragment_title_news),
                getString(R.string.fragment_title_videos),
                getString(R.string.fragment_title_blogs)
        )

        init {
            val savedFragments = mutableListOf<Fragment>()
            savedFragments.addAll(childFragmentManager.fragments.filter { it is ArticlesBaseFragment })

            fragments.add(savedFragments.firstOrNull { it is NewsFragment } ?: NewsFragment())
            fragments.add(savedFragments.firstOrNull { it is VideosFragment } ?: VideosFragment())
            fragments.add(savedFragments.firstOrNull { it is BlogsFragment } ?: BlogsFragment())
        }

        override fun getItem(position: Int): Fragment = fragments[position]

        override fun getCount(): Int = fragments.size

        override fun getPageTitle(position: Int): CharSequence? = when (getItem(position)) {
            is NewsFragment -> titles[0]
            is VideosFragment -> titles[1]
            is BlogsFragment -> titles[2]
            else -> null
        }

    }

}