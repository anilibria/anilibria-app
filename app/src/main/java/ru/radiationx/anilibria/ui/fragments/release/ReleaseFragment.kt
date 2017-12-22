package ru.radiationx.anilibria.ui.fragments.release

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.AppBarLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import kotlinx.android.synthetic.main.fragment_main_base.*
import kotlinx.android.synthetic.main.fragment_release.*
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.data.api.models.ReleaseItem
import ru.radiationx.anilibria.ui.activities.MyPlayerActivity
import ru.radiationx.anilibria.ui.common.RouterProvider
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.widgets.ScrimHelper
import ru.radiationx.anilibria.utils.Utils
import android.util.DisplayMetrics
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.toolbar_content_release.*


/* Created by radiationx on 16.11.17. */

open class ReleaseFragment : BaseFragment(), ReleaseView {
    override val layoutRes: Int = R.layout.fragment_release

    companion object {
        const val ARG_ID: String = "release_id"
        const val ARG_ITEM: String = "release_item"
    }

    private var adapter: ReleaseAdapter = ReleaseAdapter()

    @InjectPresenter
    lateinit var presenter: ReleasePresenter

    @ProvidePresenter
    fun provideReleasePresenter(): ReleasePresenter {
        return ReleasePresenter(App.injections.releasesRepository,
                (parentFragment as RouterProvider).router)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            it.getInt(ARG_ID, -1).let {
                presenter.setReleaseId(it)
            }
            (it.getSerializable(ARG_ITEM) as ReleaseItem).let {
                presenter.setCurrentData(it)
            }
        }
    }

    var currentTitle: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val params = toolbarLayout.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED
        toolbarLayout.layoutParams = params
        toolbarInsetShadow.visibility = View.VISIBLE
        toolbarContent.visibility = View.VISIBLE

        View.inflate(toolbarContent.context, R.layout.toolbar_content_release, toolbarContent)

        toolbarImage.post {

            val metrics = DisplayMetrics()
            activity?.windowManager?.defaultDisplay?.getMetrics(metrics)

            val height = metrics.heightPixels
            val width = metrics.widthPixels

            toolbarImage.maxHeight = (height * 0.75f).toInt()
        }


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

        fixToolbarInsets(toolbar)
        setMarqueeTitle(toolbar)
    }

    override fun onBackPressed(): Boolean {
        presenter.onBackPressed()
        return true
    }

    override fun setRefreshing(refreshing: Boolean) {

    }

    var currentColor: Int = Color.TRANSPARENT
    private val defaultOptionsUIL: DisplayImageOptions.Builder = DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .resetViewBeforeLoading(false)
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.ARGB_8888)
            .handler(Handler())

    override fun showRelease(release: ReleaseItem) {
        //toolbarImage.adjustViewBounds = true

        ImageLoader.getInstance().displayImage(release.image, toolbarImage, defaultOptionsUIL.build(), object : SimpleImageLoadingListener() {
            override fun onLoadingComplete(imageUri: String?, view: View?, loadedImage: Bitmap) {
                super.onLoadingComplete(imageUri, view, loadedImage)
                Single.defer {
                    Single.just(isDarkImage(loadedImage))
                }
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ isDark ->
                            toolbar?.let {
                                Log.e("SUKA", "LOADED IMAGE TYPE " + isDark)
                                if (isDark) {
                                    currentColor = Color.WHITE
                                } else {
                                    currentColor = Color.BLACK
                                }
                                toolbar.navigationIcon?.setColorFilter(currentColor, PorterDuff.Mode.SRC_ATOP)
                                toolbar.overflowIcon?.setColorFilter(currentColor, PorterDuff.Mode.SRC_ATOP)
                            }
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
        override fun onClickSd(episode: ReleaseItem.Episode, position: Int) {
            presenter.onPlayEpisodeClick(position, MyPlayerActivity.VAL_QUALITY_SD)
        }

        override fun onClickHd(episode: ReleaseItem.Episode, position: Int) {
            presenter.onPlayEpisodeClick(position, MyPlayerActivity.VAL_QUALITY_HD)
        }

        override fun onClickEpisode(episode: ReleaseItem.Episode, position: Int) {
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
                                presenter.onPlayEpisodeClick(position, quality)
                            }
                        }
                        .show()
            }
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

    override fun playEpisodes(release: ReleaseItem) {
        (recyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(1, 0)
        appbarLayout.setExpanded(false, false)
        /*startActivity(Intent(context, MyPlayerActivity::class.java).apply {
            putExtra(MyPlayerActivity.ARG_RELEASE, release)
            putExtra(MyPlayerActivity.ARG_CURRENT, 0)
        })*/
    }

    override fun playEpisode(release: ReleaseItem, position: Int, quality: Int) {
        startActivity(Intent(context, MyPlayerActivity::class.java).apply {
            putExtra(MyPlayerActivity.ARG_RELEASE, release)
            putExtra(MyPlayerActivity.ARG_CURRENT, position)
            putExtra(MyPlayerActivity.ARG_QUALITY, quality)
        })
    }

    override fun playMoonwalk(link: String) {
        Utils.externalLink(link)
    }
}
