package ru.radiationx.anilibria.ui.fragments.release.details

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_main_base.*
import kotlinx.android.synthetic.main.fragment_paged.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.presentation.release.details.ReleasePresenter
import ru.radiationx.anilibria.presentation.release.details.ReleaseView
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.SharedReceiver
import ru.radiationx.anilibria.ui.fragments.comments.LazyVkCommentsFragment
import ru.radiationx.anilibria.ui.widgets.ScrimHelper
import ru.radiationx.anilibria.utils.ShortcutHelper
import ru.radiationx.anilibria.utils.ToolbarHelper
import ru.radiationx.data.analytics.features.CommentsAnalytics
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.types.ReleaseCode
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.shared.ktx.android.gone
import ru.radiationx.shared.ktx.android.putExtra
import ru.radiationx.shared.ktx.android.visible
import ru.radiationx.shared_app.common.SystemUtils
import ru.radiationx.shared_app.di.injectDependencies
import ru.radiationx.shared_app.imageloader.showImageUrl
import javax.inject.Inject


/* Created by radiationx on 16.11.17. */
open class ReleaseFragment : BaseFragment(), ReleaseView, SharedReceiver {
    companion object {
        private const val ARG_ID: String = "release_id"
        private const val ARG_ID_CODE: String = "release_id_code"
        private const val ARG_ITEM: String = "release_item"
        const val TRANSACTION = "CHTO_TEBE_SUKA_NADO_ESHO"

        fun newInstance(
            id: ReleaseId? = null,
            code: ReleaseCode? = null,
            item: Release? = null
        ) = ReleaseFragment().putExtra {
            putParcelable(ARG_ID, id)
            putParcelable(ARG_ID_CODE, code)
            putParcelable(ARG_ITEM, item)
        }
    }

    override val needToolbarShadow: Boolean = false

    private val pagerAdapter: CustomPagerAdapter by lazy { CustomPagerAdapter() }
    private var currentColor: Int = Color.TRANSPARENT
    private var currentTitle: String? = null
    private var toolbarHelperJob: Job? = null

    @Inject
    lateinit var commentsAnalytics: CommentsAnalytics

    @Inject
    lateinit var shortcutHelper: ShortcutHelper

    @Inject
    lateinit var systemUtils: SystemUtils

    @InjectPresenter
    lateinit var presenter: ReleasePresenter

    @ProvidePresenter
    fun provideReleasePresenter(): ReleasePresenter =
        getDependency(ReleasePresenter::class.java, screenScope)

    override var transitionNameLocal = ""

    override fun setTransitionName(name: String) {
        transitionNameLocal = name
    }

    override fun getLayoutResource(): Int = R.layout.fragment_paged

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies(screenScope)
        super.onCreate(savedInstanceState)
        arguments?.also { bundle ->
            presenter.releaseId = bundle.getParcelable(ARG_ID)
            presenter.releaseIdCode = bundle.getParcelable(ARG_ID_CODE)
            presenter.argReleaseItem = bundle.getParcelable(ARG_ITEM) as? Release?
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbarImage.transitionName = transitionNameLocal
        }
        postponeEnterTransition()
        ToolbarHelper.setTransparent(toolbar, appbarLayout)
        ToolbarHelper.setScrollFlag(
            toolbarLayout,
            AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED
        )
        ToolbarHelper.fixInsets(toolbar)
        ToolbarHelper.marqueeTitle(toolbar)

        toolbar.apply {
            currentTitle = getString(R.string.fragment_title_release)
            setNavigationOnClickListener {
                presenter.onBackPressed()
            }
            setNavigationIcon(R.drawable.ic_toolbar_arrow_back)
            menu.add("Копировать ссылку")
                .setOnMenuItemClickListener {
                    presenter.onCopyLinkClick()
                    false
                }

            menu.add("Поделиться")
                .setOnMenuItemClickListener {
                    presenter.onShareClick()
                    false
                }

            menu.add("Добавить на главный экран")
                .setOnMenuItemClickListener {
                    presenter.onShortcutAddClick()
                    false
                }
        }
        toolbarInsetShadow.visible()
        toolbarImage.visible()



        toolbarImage.maxHeight = (resources.displayMetrics.heightPixels * 0.75f).toInt()

        val scrimHelper = ScrimHelper(appbarLayout, toolbarLayout)
        scrimHelper.setScrimListener(object : ScrimHelper.ScrimListener {
            override fun onScrimChanged(scrim: Boolean) {
                toolbarInsetShadow.gone(scrim)
                if (scrim) {
                    toolbar?.let {
                        it.navigationIcon?.clearColorFilter()
                        it.overflowIcon?.clearColorFilter()
                        it.title = currentTitle
                    }
                } else {
                    toolbar?.let {
                        it.navigationIcon?.setColorFilter(currentColor, PorterDuff.Mode.SRC_ATOP)
                        it.overflowIcon?.setColorFilter(currentColor, PorterDuff.Mode.SRC_ATOP)
                        it.title = null
                    }
                }
            }
        })

        viewPagerPaged.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                if (position == 1) {
                    appbarLayout.setExpanded(false)
                    presenter.onCommentsSwipe()
                }
            }
        })

        viewPagerPaged.adapter = pagerAdapter
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(ARG_ID, presenter.releaseId)
        outState.putParcelable(ARG_ID_CODE, presenter.releaseIdCode)
    }

    override fun onBackPressed(): Boolean {
        if (viewPagerPaged.currentItem > 0) {
            viewPagerPaged.currentItem = viewPagerPaged.currentItem - 1
            return true
        }
        presenter.onBackPressed()
        return true
    }

    override fun setRefreshing(refreshing: Boolean) {
        progressBarPaged.visible(refreshing)
    }

    override fun showState(state: ReleasePagerState) {
        if (state.poster == null) {
            startPostponedEnterTransition()
        } else {
            toolbarImage.showImageUrl(state.poster) {
                onStart { toolbarImageProgress?.visible() }
                onSuccess { updateToolbarColors(it) }
                onComplete {
                    toolbarImageProgress?.gone()
                    startPostponedEnterTransition()
                }
            }
        }

        if (state.title != null) {
            currentTitle = state.title
        }
    }

    override fun shareRelease(text: String) {
        systemUtils.shareText(text)
    }

    override fun copyLink(url: String) {
        systemUtils.copyToClipBoard(url)
        Toast.makeText(context, "Ссылка скопирована", Toast.LENGTH_SHORT).show()
    }

    override fun addShortCut(release: Release) {
        shortcutHelper.addShortcut(release)
    }

    override fun onDestroyView() {
        toolbarHelperJob?.cancel()
        super.onDestroyView()
    }

    private fun updateToolbarColors(loadedImage: Bitmap) {
        toolbarHelperJob?.cancel()
        toolbarHelperJob = viewLifecycleOwner.lifecycleScope.launch {
            val isDark = ToolbarHelper.isDarkImage(loadedImage)
            currentColor = if (isDark) Color.WHITE else Color.BLACK

            toolbar.navigationIcon?.setColorFilter(
                currentColor,
                PorterDuff.Mode.SRC_ATOP
            )
            toolbar.overflowIcon?.setColorFilter(
                currentColor,
                PorterDuff.Mode.SRC_ATOP
            )
        }
    }

    private inner class CustomPagerAdapter :
        androidx.fragment.app.FragmentStatePagerAdapter(
            childFragmentManager,
            BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        ) {

        private val fragments = listOf<Fragment>(
            ReleaseInfoFragment(),
            LazyVkCommentsFragment()
        )

        init {
            fragments.forEach {
                val newBundle = (this@ReleaseFragment.arguments?.clone() as Bundle?)
                it.arguments = newBundle
                it.putExtra {
                    putString(ARG_SCREEN_SCOPE, screenScope)
                }
            }
        }

        override fun getItem(position: Int): Fragment = fragments[position]

        override fun getCount(): Int = fragments.size

    }
}
