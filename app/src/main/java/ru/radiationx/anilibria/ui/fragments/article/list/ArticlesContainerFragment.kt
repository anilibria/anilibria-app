package ru.radiationx.anilibria.ui.fragments.article.list

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.fragment_article_container.*
import kotlinx.android.synthetic.main.fragment_main_base.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.common.RouterProvider
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.SharedProvider
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

            adapter = spinnerAdapter
            spinnerAdapter.setDropDownViewResource(R.layout.item_view_spinner_dropdown)

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    Log.e("S_DEF_LOG", "TEST onItemSelected " + p2)
                    val fragment = pagerAdapter.getItem(viewPager.currentItem) as ArticlesFragment
                    fragment.onSelectCategory(fragment.spinnerItems[p2].first)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
        }

        val tabLayout = TabLayout(appbarLayout.context)
        appbarLayout.addView(tabLayout)
        viewPager.adapter = pagerAdapter
        tabLayout.setupWithViewPager(viewPager)
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            var first: Boolean = true
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                if (first && positionOffset == 0f && positionOffsetPixels == 0) {
                    onPageSelected(0);
                    first = false;
                }
            }

            override fun onPageSelected(position: Int) {
                val fragment = pagerAdapter.getItem(position) as ArticlesFragment
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

    inner class CustomPagerAdapter : FragmentPagerAdapter(childFragmentManager) {
        private val fragments = listOf(
                ArticlesFragment(),
                VideosFragment(),
                BlogsFragment()
        )

        private val titles = listOf(
                getString(R.string.fragment_title_news),
                getString(R.string.fragment_title_videos),
                getString(R.string.fragment_title_blogs)
        )

        override fun getItem(position: Int): Fragment = fragments[position]

        override fun getCount(): Int = fragments.size

        override fun getPageTitle(position: Int): CharSequence? = titles[position]
    }

}