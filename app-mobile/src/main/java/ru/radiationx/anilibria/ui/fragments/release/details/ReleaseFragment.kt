package ru.radiationx.anilibria.ui.fragments.release.details

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.AppBarLayout
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.FragmentPagedBinding
import ru.radiationx.anilibria.ui.common.BackButtonListener
import ru.radiationx.anilibria.ui.fragments.BaseToolbarFragment
import ru.radiationx.anilibria.ui.fragments.SharedReceiver
import ru.radiationx.anilibria.ui.fragments.comments.LazyVkCommentsFragment
import ru.radiationx.anilibria.ui.widgets.ScrimHelper
import ru.radiationx.anilibria.utils.ShortcutHelper
import ru.radiationx.anilibria.utils.ToolbarHelper
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.types.ReleaseCode
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.quill.inject
import ru.radiationx.quill.installModules
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.getExtra
import ru.radiationx.shared.ktx.android.postopneEnterTransitionWithTimout
import ru.radiationx.shared.ktx.android.putExtra
import ru.radiationx.shared_app.common.SystemUtils
import ru.radiationx.shared_app.imageloader.showImageUrl
import java.util.concurrent.TimeUnit


/* Created by radiationx on 16.11.17. */
open class ReleaseFragment : BaseToolbarFragment<FragmentPagedBinding>(R.layout.fragment_paged),
    SharedReceiver, BackButtonListener {
    companion object {
        private const val ARG_ID: String = "release_id"
        private const val ARG_ID_CODE: String = "release_id_code"
        private const val ARG_ITEM: String = "release_item"

        private const val PAGE_INFO = 0
        private const val PAGE_COMMENTS = 1

        fun newInstance(
            id: ReleaseId? = null,
            code: ReleaseCode? = null,
            item: Release? = null,
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

    private val shortcutHelper by inject<ShortcutHelper>()

    private val systemUtils by inject<SystemUtils>()

    private val viewModel by viewModel<ReleaseViewModel> {
        ReleaseExtra(
            id = getExtra(ARG_ID),
            code = getExtra(ARG_ID_CODE),
            release = getExtra(ARG_ITEM)
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
            currentTitle = getString(R.string.fragment_title_release)
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
        scrimHelper.setScrimListener(object : ScrimHelper.ScrimListener {
            @Suppress("DEPRECATION")
            override fun onScrimChanged(scrim: Boolean) {
                baseBinding.toolbarInsetShadow.isGone = scrim
                if (scrim) {
                    baseBinding.toolbar.let {
                        it.navigationIcon?.clearColorFilter()
                        it.overflowIcon?.clearColorFilter()
                        it.title = currentTitle
                    }
                } else {
                    baseBinding.toolbar.let {
                        it.navigationIcon?.setColorFilter(currentColor, PorterDuff.Mode.SRC_ATOP)
                        it.overflowIcon?.setColorFilter(currentColor, PorterDuff.Mode.SRC_ATOP)
                        it.title = null
                    }
                }
            }
        })

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
            systemUtils.shareText(it)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.copyAction.observe().onEach {
            systemUtils.copyToClipBoard(it)
            Toast.makeText(requireContext(), "Ссылка скопирована", Toast.LENGTH_SHORT).show()
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.shortcutAction.observe().onEach {
            shortcutHelper.addShortcut(it)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.openCommentsAction.onEach {
            binding.viewPagerPaged.currentItem = PAGE_COMMENTS
        }.launchIn(viewLifecycleOwner.lifecycleScope)
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
                onSuccess { updateToolbarColors(it) }
                onComplete {
                    baseBinding.toolbarImageProgress.isGone = true
                    startPostponedEnterTransition()
                }
            }
        }

        if (state.title != null) {
            currentTitle = state.title
        }

        binding.progressBarPaged.isVisible = state.loading
    }

    override fun onDestroyView() {
        toolbarHelperJob?.cancel()
        super.onDestroyView()
    }

    @Suppress("DEPRECATION")
    private fun updateToolbarColors(loadedImage: Bitmap) {
        toolbarHelperJob?.cancel()
        toolbarHelperJob = viewLifecycleOwner.lifecycleScope.launch {
            val isDark = ToolbarHelper.isDarkImage(loadedImage)
            currentColor = if (isDark) Color.WHITE else Color.BLACK

            baseBinding.toolbar.navigationIcon?.setColorFilter(
                currentColor,
                PorterDuff.Mode.SRC_ATOP
            )
            baseBinding.toolbar.overflowIcon?.setColorFilter(
                currentColor,
                PorterDuff.Mode.SRC_ATOP
            )
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
}
