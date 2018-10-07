package ru.radiationx.anilibria.ui.fragments.release.details

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.AppBarLayout
import android.support.v4.view.PagerAdapter
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.nightlynexus.viewstatepageradapter.ViewStatePagerAdapter
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.fragment_comments.view.*
import kotlinx.android.synthetic.main.fragment_main_base.*
import kotlinx.android.synthetic.main.fragment_paged.*
import kotlinx.android.synthetic.main.fragment_release.view.*
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.release.Comment
import ru.radiationx.anilibria.entity.app.release.ReleaseFull
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.entity.app.release.TorrentItem
import ru.radiationx.anilibria.entity.app.vital.VitalItem
import ru.radiationx.anilibria.model.data.holders.PreferencesHolder
import ru.radiationx.anilibria.presentation.release.details.ReleasePresenter
import ru.radiationx.anilibria.presentation.release.details.ReleaseView
import ru.radiationx.anilibria.ui.activities.MyPlayerActivity
import ru.radiationx.anilibria.ui.activities.WebPlayerActivity
import ru.radiationx.anilibria.ui.adapters.global.CommentsAdapter
import ru.radiationx.anilibria.ui.common.RouterProvider
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.SharedReceiver
import ru.radiationx.anilibria.ui.widgets.ScrimHelper
import ru.radiationx.anilibria.ui.widgets.UniversalItemDecoration
import ru.radiationx.anilibria.utils.ToolbarHelper
import ru.radiationx.anilibria.utils.Utils
import java.net.URLConnection
import java.util.regex.Pattern


/* Created by radiationx on 16.11.17. */
@RuntimePermissions
open class ReleaseFragment : BaseFragment(), ReleaseView, SharedReceiver, ReleaseAdapter.ItemListener, CommentsAdapter.ItemListener {

    companion object {
        const val ARG_ID: String = "release_id"
        const val ARG_ID_CODE: String = "release_id_code"
        const val ARG_ITEM: String = "release_item"
        const val TRANSACTION = "CHTO_TEBE_SUKA_NADO_ESHO"
    }

    override val needToolbarShadow: Boolean = false

    private val releaseAdapter: ReleaseAdapter by lazy { ReleaseAdapter(this) }
    private val commentsAdapter: CommentsAdapter by lazy { CommentsAdapter(this) }
    private val viewPagerAdapter: CustomPagerAdapter by lazy { CustomPagerAdapter(releaseAdapter, commentsAdapter) }
    private var currentColor: Int = Color.TRANSPARENT
    private var currentTitle: String? = null

    private var toolbarHelperDisposable: Disposable? = null

    @InjectPresenter
    lateinit var presenter: ReleasePresenter

    @ProvidePresenter
    fun provideReleasePresenter(): ReleasePresenter = ReleasePresenter(
            App.injections.releaseRepository,
            App.injections.releaseInteractor,
            App.injections.historyRepository,
            App.injections.vitalRepository,
            App.injections.authRepository,
            (parentFragment as RouterProvider).getRouter(),
            App.injections.linkHandler,
            App.injections.errorHandler
    )

    override var transitionNameLocal = ""

    override fun setTransitionName(name: String) {
        transitionNameLocal = name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("S_DEF_LOG", "ONCRETE $this")
        Log.e("S_DEF_LOG", "ONCRETE REL $arguments, $savedInstanceState")
        val args = savedInstanceState ?: arguments
        args?.let {
            it.getInt(ARG_ID, -1).let { presenter.releaseId = it }
            it.getString(ARG_ID_CODE, null)?.let { presenter.releaseIdCode = it }
            it.getSerializable(ARG_ITEM)?.let {
                if (it is ReleaseFull) {
                    presenter.setLoadedData(it)
                } else if (it is ReleaseItem) {
                    presenter.setCurrentData(it)
                }
            }
        }
    }

    override fun getLayoutResource(): Int = R.layout.fragment_paged

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

        viewPager.adapter = viewPagerAdapter
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(ARG_ID, presenter.releaseId)
        outState.putString(ARG_ID_CODE, presenter.releaseIdCode)
        outState.putSerializable(ARG_ITEM, presenter.currentData)
    }

    /*private fun setupContentAnimation() {
        val animation1 = TranslateAnimation(0f, 0f, resources.displayMetrics.density * 100, 0f);
        animation1.duration = TabFragment.TRANSITION_MOVE_TIME;
        animation1.interpolator = FastOutSlowInInterpolator()
        animation1.startOffset = TabFragment.TRANSITION_OTHER_TIME;
        animation1.isFillEnabled = true
        recyclerView.startAnimation(animation1)
    }*/

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

    private val defaultOptionsUIL: DisplayImageOptions.Builder = DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .resetViewBeforeLoading(false)
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.ARGB_8888)
            .handler(Handler())

    override fun showRelease(release: ReleaseFull) {
        Log.e("S_DEF_LOG", "showRelease")
        ImageLoader.getInstance().displayImage(release.image, toolbarImage, defaultOptionsUIL.build(), object : SimpleImageLoadingListener() {
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

        currentTitle = String.format("%s / %s", release.title, release.originalTitle)
        viewPagerAdapter.showRelease(release)
    }

    override fun insertMoreComments(comments: List<Comment>) {
        commentsAdapter.addComments(comments)
    }

    override fun setEndlessComments(enable: Boolean) {
        commentsAdapter.endless = enable
    }

    override fun onLoadMore() {
        presenter.loadMoreComments()
    }

    override fun showComments(comments: List<Comment>) {
        viewPagerAdapter.showComments(comments)
    }

    override fun setCommentsRefreshing(isRefreshing: Boolean) {
        viewPagerAdapter.setCommentsRefreshing(isRefreshing)
    }

    override fun onCommentSent() {
        hideSoftwareKeyboard()
        viewPagerAdapter.clearCommentField()
    }

    override fun loadTorrent(url: String) {
        Utils.externalLink(url)
    }

    override fun shareRelease(text: String) {
        Utils.shareText(text)
    }

    override fun copyLink(url: String) {
        Utils.copyToClipBoard(url)
        Toast.makeText(context, "Ссылка скопирована", Toast.LENGTH_SHORT).show()
    }

    override fun showTorrentDialog(torrent: List<TorrentItem>) {
        val titles = torrent.map { "Серия ${it.episode} [${it.quality}][${it.size}]" }.toTypedArray()
        context?.let {
            AlertDialog.Builder(it)
                    .setItems(titles) { dialog, which ->
                        loadTorrent(torrent[which].url)
                    }
                    .show()
        }
    }

    override fun onClickSd(episode: ReleaseFull.Episode) {
        presenter.onPlayEpisodeClick(episode, MyPlayerActivity.VAL_QUALITY_SD)
    }

    override fun onClickHd(episode: ReleaseFull.Episode) {
        presenter.onPlayEpisodeClick(episode, MyPlayerActivity.VAL_QUALITY_HD)
    }

    override fun onClickEpisode(episode: ReleaseFull.Episode) {
        presenter.onPlayEpisodeClick(episode)
    }

    override fun onClickTorrent(url: String?) {
        presenter.onTorrentClick()
    }

    override fun onClickWatchAll() {
        presenter.onPlayAllClick()
    }

    override fun onClickContinue() {
        presenter.onClickContinue()
    }

    override fun onClickTag(text: String) {
        presenter.openSearch(text)
    }

    override fun onClickFav() {
        presenter.onClickFav()
    }

    override fun onClickSomeLink(url: String): Boolean {
        val handled = presenter.onClickLink(url)
        if (!handled) {
            Utils.externalLink(url)
        }
        return true
    }

    override fun onClickDonate() {
        presenter.onClickDonate()
    }

    override fun playEpisodes(release: ReleaseFull) {
        /*selectQuality({ quality ->
            playEpisode(release, release.episodes.last(), quality)
        })*/
        playEpisode(release, release.episodes.last())
    }

    override fun playContinue(release: ReleaseFull, startWith: ReleaseFull.Episode) {
        /*selectQuality({ quality ->
            playEpisode(release, startWith, quality, MyPlayerActivity.PLAY_FLAG_FORCE_CONTINUE)
        })*/
        playEpisode(release, startWith, MyPlayerActivity.PLAY_FLAG_FORCE_CONTINUE)
    }

    private fun getUrlByQuality(episode: ReleaseFull.Episode, quality: Int): String {
        return when (quality) {
            0 -> episode.urlSd
            1 -> episode.urlHd
            else -> episode.urlSd
        }.orEmpty()
    }

    private fun showDownloadDialog(url: String) {
        val titles = arrayOf("Внешний загрузчик", "Системный загрузчик")
        context?.let {
            AlertDialog.Builder(it)
                    .setItems(titles) { dialog, which ->
                        when (which) {
                            0 -> Utils.externalLink(url)
                            1 -> systemDownloadWithPermissionCheck(url)
                        }
                    }
                    .show()
        }
    }

    override fun playEpisode(release: ReleaseFull, episode: ReleaseFull.Episode, playFlag: Int?, quality: Int?) {
        if (episode.type == ReleaseFull.Episode.Type.SOURCE) {
            if (quality == null) {
                selectQuality({ selected ->
                    showDownloadDialog(getUrlByQuality(episode, selected))
                }, true)
            } else {
                showDownloadDialog(getUrlByQuality(episode, quality))
            }
        } else {
            val titles = arrayOf("Внешний плеер", "Внутренний плеер")
            context?.let {
                AlertDialog.Builder(it)
                        .setItems(titles) { dialog, which ->
                            if (quality == null) {
                                when (which) {
                                    0 -> {
                                        selectQuality({ selected ->
                                            playExternal(release, episode, selected)
                                        }, true)
                                    }
                                    1 -> {
                                        selectQuality({ selected ->
                                            playInternal(release, episode, selected, playFlag)
                                        })
                                    }
                                }
                            } else {
                                when (which) {
                                    0 -> playExternal(release, episode, quality)
                                    1 -> playInternal(release, episode, quality, playFlag)
                                }
                            }
                        }
                        .show()
            }
        }
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun systemDownload(url: String) {
        var fileName = Utils.getFileNameFromUrl(url)
        val matcher = Pattern.compile("\\?download=([\\s\\S]+)").matcher(fileName)
        if (matcher.find()) {
            fileName = matcher.group(1)
        }
        this.context?.let { Utils.systemDownloader(it, url, fileName) }
    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    private fun playInternal(release: ReleaseFull, episode: ReleaseFull.Episode, quality: Int, playFlag: Int? = null) {
        startActivity(Intent(context, MyPlayerActivity::class.java).apply {
            putExtra(MyPlayerActivity.ARG_RELEASE, release)
            putExtra(MyPlayerActivity.ARG_EPISODE_ID, episode.id)
            putExtra(MyPlayerActivity.ARG_QUALITY, quality)
            playFlag?.let {
                putExtra(MyPlayerActivity.ARG_PLAY_FLAG, it)
            }
        })
    }

    private fun playExternal(release: ReleaseFull, episode: ReleaseFull.Episode, quality: Int) {
        presenter.markEpisodeViewed(episode)
        val url = when (quality) {
            0 -> episode.urlSd
            1 -> episode.urlHd
            else -> episode.urlSd
        }
        val fileUri = Uri.parse(url)
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.setDataAndType(fileUri, URLConnection.guessContentTypeFromName(fileUri.toString()))
        try {
            startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(context, "Ничего не найдено", Toast.LENGTH_SHORT).show()
        }
    }

    override fun playWeb(link: String) {
        //Utils.externalLink(link)
        startActivity(Intent(context, WebPlayerActivity::class.java).apply {
            putExtra(WebPlayerActivity.ARG_URL, link)
        })
    }

    override fun onClickWatchWeb() {
        presenter.onClickWatchWeb()
    }

    private fun selectQuality(onSelect: (quality: Int) -> Unit, forceDialog: Boolean = false) {
        if (forceDialog) {
            showQualityDialog(onSelect, false)
        } else {
            val savedQuality = presenter.getQuality()
            when (savedQuality) {
                PreferencesHolder.QUALITY_NO -> {
                    showQualityDialog(onSelect)
                }
                PreferencesHolder.QUALITY_ALWAYS -> {
                    showQualityDialog(onSelect, false)
                }
                PreferencesHolder.QUALITY_SD -> {
                    onSelect(MyPlayerActivity.VAL_QUALITY_SD)
                }
                PreferencesHolder.QUALITY_HD -> {
                    onSelect(MyPlayerActivity.VAL_QUALITY_HD)
                }
            }
        }
    }

    private fun showQualityDialog(onSelect: (quality: Int) -> Unit, saveQuality: Boolean = true) {
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
                            if (saveQuality) {
                                presenter.setQuality(when (quality) {
                                    MyPlayerActivity.VAL_QUALITY_SD -> PreferencesHolder.QUALITY_SD
                                    MyPlayerActivity.VAL_QUALITY_HD -> PreferencesHolder.QUALITY_HD
                                    else -> PreferencesHolder.QUALITY_NO
                                })
                            }
                            onSelect.invoke(quality)
                        }
                    }
                    .show()
        }
    }

    override fun updateFavCounter() {
        releaseAdapter.notifyDataSetChanged()
    }

    override fun showVitalItems(vital: List<VitalItem>) {
        releaseAdapter.setVitals(vital)
    }

    override fun showFavoriteDialog() {
        context?.let {
            AlertDialog.Builder(it)
                    .setMessage("Для выполнения действия необходимо авторизоваться. Авторизоваться?")
                    .setPositiveButton("Да") { dialog, which -> presenter.openAuth() }
                    .setNegativeButton("Нет", null)
                    .show()
        }
    }

    override fun onDestroyView() {
        toolbarHelperDisposable?.dispose()
        toolbarHelperDisposable = null
        super.onDestroyView()
    }


    private inner class CustomPagerAdapter(
            private val releaseAdapter: ReleaseAdapter,
            private val commentsAdapter: CommentsAdapter
    ) : ViewStatePagerAdapter() {

        private val views = arrayOf(R.layout.fragment_release, R.layout.fragment_comments)
        private var localCommentsRootLayout: ViewGroup? = null

        override fun createView(container: ViewGroup, position: Int): View {
            val inflater: LayoutInflater = LayoutInflater.from(container.context)
            val layout: ViewGroup = inflater.inflate(views[position], container, false) as ViewGroup
            container.addView(layout)
            if (position == 0) {
                createMain(layout)
            } else if (position == 1) {
                createComments(layout)
            }
            return layout
        }

        override fun getCount(): Int = 2

        private fun createMain(layout: ViewGroup) {
            layout.run {
                recyclerView.apply {
                    setHasFixedSize(true)
                    adapter = this@CustomPagerAdapter.releaseAdapter
                    layoutManager = LinearLayoutManager(this.context)
                }
            }
        }

        private fun createComments(layout: ViewGroup) {
            layout.apply {
                localCommentsRootLayout = commentsRootLayout
                commentsRefreshLayout.setOnRefreshListener {
                    presenter.reloadComments()
                }
                commentsRecyclerView.apply {
                    adapter = this@CustomPagerAdapter.commentsAdapter
                    layoutManager = LinearLayoutManager(this.context)
                    //addItemDecoration(ru.radiationx.anilibria.ui.widgets.DividerItemDecoration(this.context))
                    addItemDecoration(UniversalItemDecoration().fullWidth(true).spacingDp(1f).includeEdge(false))

                    addOnScrollListener(object : RecyclerView.OnScrollListener() {
                        override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                            if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                                hideSoftwareKeyboard()
                                localCommentsRootLayout?.commentField?.clearFocus()
                            }
                        }
                    })
                }
                commentSend.setOnClickListener {
                    presenter.onClickSendComment(commentField.text?.toString()?.trim().orEmpty())
                }
            }
        }

        fun showRelease(release: ReleaseFull) {
            releaseAdapter.setRelease(release)
        }

        fun showComments(comments: List<Comment>) {
            commentsAdapter.setComments(comments)
        }

        fun setCommentsRefreshing(isRefreshing: Boolean) {
            localCommentsRootLayout?.commentsRefreshLayout?.isRefreshing = isRefreshing
        }

        fun clearCommentField() {
            localCommentsRootLayout?.commentField?.text?.clear()
        }
    }
}
