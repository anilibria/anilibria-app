package ru.radiationx.anilibria.ui.fragments.release

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.AppBarLayout
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.animation.TranslateAnimation
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.fragment_main_base.*
import kotlinx.android.synthetic.main.fragment_release.*
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.data.api.models.release.ReleaseFull
import ru.radiationx.anilibria.data.api.models.release.ReleaseItem
import ru.radiationx.anilibria.ui.activities.MyPlayerActivity
import ru.radiationx.anilibria.ui.common.RouterProvider
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.SharedReceiver
import ru.radiationx.anilibria.ui.fragments.TabFragment
import ru.radiationx.anilibria.ui.widgets.ScrimHelper
import ru.radiationx.anilibria.utils.ToolbarHelper
import ru.radiationx.anilibria.utils.Utils


/* Created by radiationx on 16.11.17. */

open class ReleaseFragment : BaseFragment(), ReleaseView, SharedReceiver {

    companion object {
        const val ARG_ID: String = "release_id"
        const val ARG_ITEM: String = "release_item"
        const val TRANSACTION = "CHTO_TEBE_SUKA_NADO_ESHO"
    }

    private var adapter: ReleaseAdapter = ReleaseAdapter()
    private var currentColor: Int = Color.TRANSPARENT
    private var currentTitle: String? = null

    @InjectPresenter
    lateinit var presenter: ReleasePresenter

    @ProvidePresenter
    fun provideReleasePresenter(): ReleasePresenter {
        return ReleasePresenter(App.injections.releaseRepository,
                (parentFragment as RouterProvider).router)
    }

    override var transitionNameLocal = ""

    override fun setTransitionName(name: String) {
        transitionNameLocal = name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("SUKA", "ONCRETE $this")
        if (savedInstanceState == null) {
            arguments?.let {
                it.getInt(ARG_ID, -1).let { presenter.setReleaseId(it) }
                (it.getSerializable(ARG_ITEM) as ReleaseItem).let { presenter.setCurrentData(it) }
            }
        }
    }

    override fun getLayoutResource(): Int = R.layout.fragment_release

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ToolbarHelper.setTransparent(toolbar, appbarLayout)
        ToolbarHelper.setScrollFlag(toolbarLayout, AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED)
        ToolbarHelper.fixInsets(toolbar)
        ToolbarHelper.marqueeTitle(toolbar)

        toolbar.apply {
            currentTitle = getString(R.string.fragment_title_release)
            setNavigationOnClickListener({
                presenter.onBackPressed()
            })
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
        }
        toolbarInsetShadow.visibility = View.VISIBLE
        toolbarImage.visibility = View.VISIBLE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbarImage.transitionName = transitionNameLocal
        }

        val metrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(metrics)
        toolbarImage.maxHeight = (metrics.heightPixels * 0.75f).toInt()

        val scrimHelper = ScrimHelper(appbarLayout, toolbarLayout)
        scrimHelper.setScrimListener(object : ScrimHelper.ScrimListener {
            override fun onScrimChanged(scrim: Boolean) {
                if (scrim) {
                    toolbar.navigationIcon?.clearColorFilter()
                    toolbar.overflowIcon?.clearColorFilter()
                    toolbar.title = currentTitle
                    //toolbarTitleView.setVisibility(View.VISIBLE)

                    toolbarInsetShadow.visibility = View.GONE
                } else {
                    toolbar.navigationIcon?.setColorFilter(currentColor, PorterDuff.Mode.SRC_ATOP)
                    toolbar.overflowIcon?.setColorFilter(currentColor, PorterDuff.Mode.SRC_ATOP)
                    toolbar.title = null
                    //toolbarTitleView.setVisibility(View.GONE)

                    toolbarInsetShadow.visibility = View.VISIBLE
                }
            }
        })

        adapter.setReleaseListener(releaseListener)
        recyclerView.apply {
            setHasFixedSize(true)
            adapter = this@ReleaseFragment.adapter
            layoutManager = LinearLayoutManager(recyclerView.context)
        }
    }

    private fun setupContentAnimation() {
        val animation1 = TranslateAnimation(0f, 0f, resources.displayMetrics.density * 100, 0f);
        animation1.duration = TabFragment.TRANSITION_MOVE_TIME;
        animation1.interpolator = FastOutSlowInInterpolator()
        animation1.startOffset = TabFragment.TRANSITION_OTHER_TIME;
        animation1.isFillEnabled = true
        recyclerView.startAnimation(animation1)
    }

    override fun onBackPressed(): Boolean {
        presenter.onBackPressed()
        return true
    }

    override fun setRefreshing(refreshing: Boolean) {

    }

    private val defaultOptionsUIL: DisplayImageOptions.Builder = DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .resetViewBeforeLoading(false)
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.ARGB_8888)
            .handler(Handler())

    override fun showRelease(release: ReleaseFull) {
        ImageLoader.getInstance().displayImage(release.image, toolbarImage, defaultOptionsUIL.build(), object : SimpleImageLoadingListener() {
            override fun onLoadingComplete(imageUri: String?, view: View?, loadedImage: Bitmap) {
                super.onLoadingComplete(imageUri, view, loadedImage)
                ToolbarHelper.isDarkImage(loadedImage, Consumer<Boolean> {
                    if (it) {
                        currentColor = Color.WHITE
                    } else {
                        currentColor = Color.BLACK
                    }
                    toolbar.navigationIcon?.setColorFilter(currentColor, PorterDuff.Mode.SRC_ATOP)
                    toolbar.overflowIcon?.setColorFilter(currentColor, PorterDuff.Mode.SRC_ATOP)
                })
            }
        })

        currentTitle = String.format("%s / %s", release.title, release.originalTitle)
        adapter.setRelease(release)
        adapter.notifyDataSetChanged()
    }

    override fun loadTorrent(url: String) {
        Utils.externalLink(url)
    }

    override fun shareRelease(text: String) {
        Utils.shareText(text)
    }

    override fun copyLink(url: String) {
        Utils.externalLink(url)
    }

    private val releaseListener = object : ReleaseAdapter.ReleaseListener {
        override fun onClickSd(episode: ReleaseFull.Episode, position: Int) {
            presenter.onPlayEpisodeClick(position, MyPlayerActivity.VAL_QUALITY_SD)
        }

        override fun onClickHd(episode: ReleaseFull.Episode, position: Int) {
            presenter.onPlayEpisodeClick(position, MyPlayerActivity.VAL_QUALITY_HD)
        }

        override fun onClickEpisode(episode: ReleaseFull.Episode, position: Int) {
            showQualityDialog({ quality ->
                presenter.onPlayEpisodeClick(position, quality)
            })
        }

        override fun onClickTorrent(url: String) {
            presenter.onTorrentClick()
        }

        override fun onClickWatchAll() {
            presenter.onPlayAllClick()
        }

        override fun onClickTag(text: String) {
            presenter.openSearch(text)
        }
    }

    override fun playEpisodes(release: ReleaseFull) {

        showQualityDialog({ quality ->
            presenter.onPlayEpisodeClick(release.episodes.lastIndex, quality)
        })
    }

    override fun playEpisode(release: ReleaseFull, position: Int, quality: Int) {
        Log.e("SUKA", "playEpisode " + release.episodes.size)
        startActivity(Intent(context, MyPlayerActivity::class.java).apply {
            putExtra(MyPlayerActivity.ARG_RELEASE, release)
            putExtra(MyPlayerActivity.ARG_CURRENT, position)
            putExtra(MyPlayerActivity.ARG_QUALITY, quality)
        })
    }

    override fun playMoonwalk(link: String) {
        Utils.externalLink(link)
    }

    fun showQualityDialog(onSelect: (quality: Int) -> Unit) {
        context?.let {
            AlertDialog.Builder(it)
                    .setTitle("Качество")
                    .setItems(arrayOf("SD", "HD")) { p0, p1 ->
                        val quality: Int = when (p1) {
                            0 -> MyPlayerActivity.VAL_QUALITY_SD
                            1 -> MyPlayerActivity.VAL_QUALITY_HD
                            else -> -1
                        }
                        if (quality != -1) {
                            onSelect.invoke(quality)
                        }
                    }
                    .show()
        }
    }
}
