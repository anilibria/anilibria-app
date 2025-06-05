package ru.radiationx.anilibria.ui.fragments.release.details

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.AppBarLayout
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.FragmentPagedBinding
import ru.radiationx.anilibria.ui.common.BackButtonListener
import ru.radiationx.anilibria.ui.fragments.BaseToolbarFragment
import ru.radiationx.anilibria.ui.fragments.SharedReceiver
import ru.radiationx.anilibria.ui.fragments.TopScroller
import ru.radiationx.anilibria.ui.fragments.comments.LazyVkCommentsFragment
import ru.radiationx.anilibria.ui.widgets.ScrimHelper
import ru.radiationx.anilibria.utils.ShortcutHelper
import ru.radiationx.anilibria.utils.ToolbarHelper
import ru.radiationx.data.api.releases.models.Release
import ru.radiationx.data.common.ReleaseId
import ru.radiationx.quill.inject
import ru.radiationx.quill.installModules
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.getExtra
import ru.radiationx.shared.ktx.android.getExtraNotNull
import ru.radiationx.shared.ktx.android.launchInResumed
import ru.radiationx.shared.ktx.android.postopneEnterTransitionWithTimout
import ru.radiationx.shared.ktx.android.putExtra
import ru.radiationx.shared_app.common.SystemUtils
import ru.radiationx.shared_app.imageloader.showImageUrl


/* Created by radiationx on 16.11.17. */
open class ReleaseFragment : BaseToolbarFragment<FragmentPagedBinding>(R.layout.fragment_paged),
    SharedReceiver, BackButtonListener, TopScroller {
    companion object {
        private const val ARG_ID: String = "release_id"
        private const val ARG_RELEASE: String = "release_item"

        private const val PAGE_INFO = 0
        private const val PAGE_COMMENTS = 1

        fun newInstance(
            id: ReleaseId,
            release: Release?,
        ) = ReleaseFragment().putExtra {
            putParcelable(ARG_ID, id)
            putParcelable(ARG_RELEASE, release)
        }
    }

    override val needToolbarShadow: Boolean = false

    private val pagerAdapter: CustomPagerAdapter by lazy { CustomPagerAdapter() }
    private val topAppearanceState = MutableStateFlow(TopAppearance())
    private var toolbarHelperJob: Job? = null

    private val windowInsetsController by lazy {
        requireActivity().window.let {
            WindowCompat.getInsetsController(it, it.decorView)
        }
    }

    private val shortcutHelper by inject<ShortcutHelper>()

    private val systemUtils by inject<SystemUtils>()

    private val viewModel by viewModel<ReleaseViewModel> {
        ReleaseExtra(
            id = getExtraNotNull(ARG_ID),
            release = getExtra(ARG_RELEASE)
        )
    }

    override var transitionNameLocal = ""

    override fun setTransitionName(name: String) {
        transitionNameLocal = name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installModules(ReleaseModule())
        super.onCreate(savedInstanceState)
    }

    override fun onCreateBinding(view: View): FragmentPagedBinding {
        return FragmentPagedBinding.bind(view)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        topAppearanceState.update {
            it.copy(
                defaultLightStatusBar = windowInsetsController.isAppearanceLightStatusBars,
                title = getString(R.string.fragment_title_release)
            )
        }
        baseBinding.toolbarImage.transitionName = transitionNameLocal
        postopneEnterTransitionWithTimout()
        ToolbarHelper.setTransparent(baseBinding.toolbar, baseBinding.appbarLayout)
        ToolbarHelper.setScrollFlag(
            baseBinding.toolbarLayout,
            AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED
        )
        ToolbarHelper.fixInsets(baseBinding.toolbar)
        ToolbarHelper.marqueeTitle(baseBinding.toolbar)

        baseBinding.toolbar.apply {
            setNavigationOnClickListener {
                viewModel.onBackPressed()
            }
            setNavigationIcon(R.drawable.ic_toolbar_arrow_back)
            menu.add("Копировать ссылку")
                .setOnMenuItemClickListener {
                    viewModel.onCopyLinkClick()
                    false
                }

            menu.add("Поделиться")
                .setOnMenuItemClickListener {
                    viewModel.onShareClick()
                    false
                }

            menu.add("Добавить на главный экран")
                .setOnMenuItemClickListener {
                    viewModel.onShortcutAddClick()
                    false
                }
        }
        baseBinding.toolbarInsetShadow.isVisible = true
        baseBinding.toolbarImage.isVisible = true

        baseBinding.toolbarImage.maxHeight = (resources.displayMetrics.heightPixels * 0.75f).toInt()

        val scrimHelper = ScrimHelper(baseBinding.appbarLayout, baseBinding.toolbarLayout)

        combine(
            topAppearanceState,
            scrimHelper.scrimState
        ) { topAppearance, scrim ->
            if (topAppearance.isFragmentHidden || topAppearance.isDarkImage == null || scrim) {
                TopAppearanceStyle(
                    iconColor = null,
                    title = topAppearance.title,
                    lightStatusBar = topAppearance.defaultLightStatusBar,
                    scrim = true
                )
            } else {
                val iconColor = (if (topAppearance.isDarkImage) Color.WHITE else Color.BLACK)
                TopAppearanceStyle(
                    iconColor = iconColor,
                    title = null,
                    lightStatusBar = !topAppearance.isDarkImage,
                    scrim = false
                )
            }
        }.distinctUntilChanged()
            .onEach { style ->
                baseBinding.toolbarInsetShadow.isGone = style.scrim

                val iconColorFilter = style.iconColor?.let {
                    PorterDuffColorFilter(it, PorterDuff.Mode.SRC_ATOP)
                }

                baseBinding.toolbar.let {
                    it.navigationIcon?.colorFilter = iconColorFilter
                    it.overflowIcon?.colorFilter = iconColorFilter
                    it.title = style.title
                    windowInsetsController.isAppearanceLightStatusBars = style.lightStatusBar
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        binding.viewPagerPaged.addOnPageChangeListener(object :
            ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                if (position == PAGE_COMMENTS) {
                    baseBinding.appbarLayout.setExpanded(false)
                    viewModel.onCommentsSwipe()
                }
            }
        })

        binding.viewPagerPaged.adapter = pagerAdapter

        viewModel.state.onEach {
            showState(it)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.shareAction.observe().onEach {
            systemUtils.share(it)
        }.launchInResumed(viewLifecycleOwner)

        viewModel.copyAction.observe().onEach {
            systemUtils.copy(it)
            Toast.makeText(requireContext(), "Ссылка скопирована", Toast.LENGTH_SHORT).show()
        }.launchInResumed(viewLifecycleOwner)

        viewModel.shortcutAction.observe().onEach {
            shortcutHelper.addShortcut(it)
        }.launchInResumed(viewLifecycleOwner)

        viewModel.openCommentsAction.onEach {
            binding.viewPagerPaged.currentItem = PAGE_COMMENTS
        }.launchInResumed(viewLifecycleOwner)

    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        topAppearanceState.update {
            it.copy(isFragmentHidden = hidden)
        }
    }

    override fun onBackPressed(): Boolean {
        if (binding.viewPagerPaged.currentItem != PAGE_INFO) {
            binding.viewPagerPaged.currentItem = PAGE_INFO
            return true
        }
        return false
    }

    private fun showState(state: ReleasePagerState) {
        if (state.poster == null) {
            startPostponedEnterTransition()
        } else {
            baseBinding.toolbarImage.showImageUrl(state.poster) {
                onStart { baseBinding.toolbarImageProgress.isVisible = true }
                onSuccess {
                    if (it != null) {
                        updateToolbarColors(it)
                    }
                }
                onComplete {
                    baseBinding.toolbarImageProgress.isGone = true
                    startPostponedEnterTransition()
                }
            }
        }

        if (state.title != null) {
            topAppearanceState.update {
                it.copy(title = state.title)
            }
        }

        binding.progressBarPaged.isVisible = state.loading
    }

    override fun onDestroyView() {
        windowInsetsController.isAppearanceLightStatusBars =
            topAppearanceState.value.defaultLightStatusBar
        super.onDestroyView()
    }

    override fun scrollToTop() {
        baseBinding.appbarLayout.setExpanded(true, true)
        val position = binding.viewPagerPaged.currentItem
        val fragment = pagerAdapter.instantiateItem(binding.viewPagerPaged, position)
        if (fragment is TopScroller) {
            fragment.scrollToTop()
        }
    }

    @Suppress("DEPRECATION")
    private fun updateToolbarColors(loadedImage: Bitmap) {
        toolbarHelperJob?.cancel()
        toolbarHelperJob = viewLifecycleOwner.lifecycleScope.launch {
            val isDark = ToolbarHelper.isDarkImage(loadedImage)
            topAppearanceState.update {
                it.copy(isDarkImage = isDark)
            }
        }
    }

    @Suppress("DEPRECATION")
    private inner class CustomPagerAdapter :
        androidx.fragment.app.FragmentStatePagerAdapter(
            childFragmentManager,
            BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        ) {

        private val fragments = listOf<Fragment>(
            ReleaseInfoFragment(),
            LazyVkCommentsFragment()
        )

        override fun getItem(position: Int): Fragment = fragments[position]

        override fun getCount(): Int = fragments.size

    }

    private data class TopAppearance(
        val isFragmentHidden: Boolean = false,
        val isDarkImage: Boolean? = null,
        val title: String = "",
        val defaultLightStatusBar: Boolean = false
    )

    private data class TopAppearanceStyle(
        val iconColor: Int?,
        val title: String?,
        val lightStatusBar: Boolean,
        val scrim: Boolean
    )
}
