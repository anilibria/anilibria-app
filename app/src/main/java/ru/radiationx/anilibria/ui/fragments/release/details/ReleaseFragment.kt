package ru.radiationx.anilibria.ui.fragments.release.details

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import android.util.Log
import android.view.View
import android.widget.Toast
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.fragment_main_base.*
import kotlinx.android.synthetic.main.fragment_paged.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.di.extensions.getDependency
import ru.radiationx.anilibria.di.extensions.injectDependencies
import ru.radiationx.anilibria.entity.app.release.ReleaseFull
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.extension.putExtra
import ru.radiationx.anilibria.presentation.release.details.ReleasePresenter
import ru.radiationx.anilibria.presentation.release.details.ReleaseView
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.SharedReceiver
import ru.radiationx.anilibria.ui.fragments.comments.vk.VkCommentsFragment
import ru.radiationx.anilibria.ui.widgets.ScrimHelper
import ru.radiationx.anilibria.utils.ShortcutHelper
import ru.radiationx.anilibria.utils.ToolbarHelper
import ru.radiationx.anilibria.utils.Utils


/* Created by radiationx on 16.11.17. */
open class ReleaseFragment : BaseFragment(), ReleaseView, SharedReceiver {
    companion object {
        private const val ARG_ID: String = "release_id"
        private const val ARG_ID_CODE: String = "release_id_code"
        private const val ARG_ITEM: String = "release_item"
        const val TRANSACTION = "CHTO_TEBE_SUKA_NADO_ESHO"

        fun newInstance(
                id: Int = -1,
                code: String? = null,
                item: ReleaseItem? = null
        ) = ReleaseFragment().putExtra {
            putInt(ReleaseFragment.ARG_ID, id)
            putString(ReleaseFragment.ARG_ID_CODE, code)
            putSerializable(ReleaseFragment.ARG_ITEM, item)
        }
    }

    override val needToolbarShadow: Boolean = false

    private val pagerAdapter: CustomPagerAdapter by lazy { CustomPagerAdapter() }
    private var currentColor: Int = Color.TRANSPARENT
    private var currentTitle: String? = null
    private var toolbarHelperDisposable: Disposable? = null

    private val defaultOptionsUIL: DisplayImageOptions.Builder = DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .resetViewBeforeLoading(false)
            .cacheInMemory()
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.ARGB_8888)

    @InjectPresenter
    lateinit var presenter: ReleasePresenter

    @ProvidePresenter
    fun provideReleasePresenter(): ReleasePresenter = getDependency(screenScope, ReleasePresenter::class.java)

    override var transitionNameLocal = ""

    override fun setTransitionName(name: String) {
        transitionNameLocal = name
    }

    override fun getLayoutResource(): Int = R.layout.fragment_paged

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies(screenScope)
        super.onCreate(savedInstanceState)
        Log.e("S_DEF_LOG", "ONCRETE $this")
        Log.e("S_DEF_LOG", "ONCRETE REL $arguments, $savedInstanceState")
        arguments?.also { bundle ->
            presenter.releaseId = bundle.getInt(ARG_ID, presenter.releaseId)
            presenter.releaseIdCode = bundle.getString(ARG_ID_CODE, presenter.releaseIdCode)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ToolbarHelper.setTransparent(toolbar, appbarLayout)
        ToolbarHelper.setScrollFlag(toolbarLayout, AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED)
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
        toolbarInsetShadow.visibility = View.VISIBLE
        toolbarImage.visibility = View.VISIBLE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbarImage.transitionName = transitionNameLocal
        }

        toolbarImage.maxHeight = (resources.displayMetrics.heightPixels * 0.75f).toInt()

        val scrimHelper = ScrimHelper(appbarLayout, toolbarLayout)
        scrimHelper.setScrimListener(object : ScrimHelper.ScrimListener {
            override fun onScrimChanged(scrim: Boolean) {
                if (scrim) {
                    toolbar?.let {
                        it.navigationIcon?.clearColorFilter()
                        it.overflowIcon?.clearColorFilter()
                        it.title = currentTitle
                        toolbarInsetShadow.visibility = View.GONE
                    }
                } else {
                    toolbar?.let {
                        it.navigationIcon?.setColorFilter(currentColor, PorterDuff.Mode.SRC_ATOP)
                        it.overflowIcon?.setColorFilter(currentColor, PorterDuff.Mode.SRC_ATOP)
                        it.title = null
                        toolbarInsetShadow.visibility = View.VISIBLE
                    }
                }
            }
        })

        viewPager.adapter = pagerAdapter
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(ARG_ID, presenter.releaseId)
        outState.putString(ARG_ID_CODE, presenter.releaseIdCode)
    }

    override fun onBackPressed(): Boolean {
        if (viewPager.currentItem > 0) {
            viewPager.currentItem = viewPager.currentItem - 1
            return true
        }
        presenter.onBackPressed()
        return true
    }

    override fun setRefreshing(refreshing: Boolean) {
        progressBar.visibility = if (refreshing) View.VISIBLE else View.GONE
    }

    override fun showRelease(release: ReleaseFull) {
        Log.e("S_DEF_LOG", "showRelease")
        ImageLoader.getInstance().displayImage(release.poster, toolbarImage, defaultOptionsUIL.build(), object : SimpleImageLoadingListener() {
            override fun onLoadingComplete(imageUri: String?, view: View?, loadedImage: Bitmap) {
                super.onLoadingComplete(imageUri, view, loadedImage)
                if (toolbarHelperDisposable == null) {
                    toolbarHelperDisposable = ToolbarHelper.isDarkImage(loadedImage, Consumer {
                        currentColor = if (it) Color.WHITE else Color.BLACK

                        toolbar.navigationIcon?.setColorFilter(currentColor, PorterDuff.Mode.SRC_ATOP)
                        toolbar.overflowIcon?.setColorFilter(currentColor, PorterDuff.Mode.SRC_ATOP)
                    })
                }
            }
        })

        currentTitle = String.format("%s / %s", release.title, release.titleEng)
    }

    override fun shareRelease(text: String) {
        Utils.shareText(text)
    }

    override fun copyLink(url: String) {
        Utils.copyToClipBoard(url)
        Toast.makeText(context, "Ссылка скопирована", Toast.LENGTH_SHORT).show()
    }

    override fun addShortCut(release: ReleaseItem) {
        ShortcutHelper.addShortcut(release)
    }

    override fun onDestroyView() {
        toolbarHelperDisposable?.dispose()
        super.onDestroyView()
    }

    private inner class CustomPagerAdapter() : FragmentStatePagerAdapter(childFragmentManager) {

        private val fragments = listOf<Fragment>(
                ReleaseInfoFragment()/*,
                CommentsFragment()*/,
                VkCommentsFragment()
        )

        init {
            fragments.forEach {
                val newBundle = (this@ReleaseFragment.arguments?.clone() as Bundle?)
                it.arguments = newBundle
                it.putExtra {
                    putString(BaseFragment.ARG_SCREEN_SCOPE, screenScope)
                }
                Log.e("lalallala", "CustomPagerAdapter ini ${newBundle}")
            }
        }

        override fun getItem(position: Int): Fragment = fragments[position]

        override fun getCount(): Int = fragments.size

    }
}
