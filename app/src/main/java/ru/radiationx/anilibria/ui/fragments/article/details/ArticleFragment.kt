package ru.radiationx.anilibria.ui.fragments.article.details

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.view.PagerAdapter
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import android.widget.ViewSwitcher
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.nightlynexus.viewstatepageradapter.ViewStatePagerAdapter
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.fragment_article.view.*
import kotlinx.android.synthetic.main.fragment_comments.view.*
import kotlinx.android.synthetic.main.fragment_main_base.*
import kotlinx.android.synthetic.main.fragment_paged.*
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.article.ArticleItem
import ru.radiationx.anilibria.entity.app.release.Comment
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.presentation.article.details.ArticlePresenter
import ru.radiationx.anilibria.presentation.article.details.ArticleView
import ru.radiationx.anilibria.ui.adapters.global.CommentsAdapter
import ru.radiationx.anilibria.ui.common.RouterProvider
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.SharedReceiver
import ru.radiationx.anilibria.ui.widgets.ExtendedWebView
import ru.radiationx.anilibria.ui.widgets.ScrimHelper
import ru.radiationx.anilibria.ui.widgets.UniversalItemDecoration
import ru.radiationx.anilibria.utils.ToolbarHelper
import ru.radiationx.anilibria.utils.Utils
import java.nio.charset.StandardCharsets
import java.util.*


/**
 * Created by radiationx on 20.12.17.
 */
class ArticleFragment : BaseFragment(), ArticleView, SharedReceiver, CommentsAdapter.ItemListener {
    companion object {
        const val ARG_ITEM: String = "article_item"
        const val ARG_ID_NAME: String = "article_id_name"
        private const val WEB_VIEW_SCROLL_Y = "wvsy"
    }

    override val needToolbarShadow: Boolean = false

    private var currentColor: Int = Color.TRANSPARENT
    private var currentTitle: String? = null

    private val commentsAdapter = CommentsAdapter(this)
    private val pagerAdapter: CustomPagerAdapter = CustomPagerAdapter(commentsAdapter)

    private var toolbarHelperDisposable: Disposable? = null

    @InjectPresenter
    lateinit var presenter: ArticlePresenter

    @ProvidePresenter
    fun provideArticlePresenter(): ArticlePresenter = ArticlePresenter(
            App.injections.articleRepository,
            App.injections.vitalRepository,
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
        val args = savedInstanceState ?: arguments
        args?.let {
            it.getString(ARG_ID_NAME, null)?.let { presenter.articleIdCode = it }
            (it.getSerializable(ARG_ITEM) as ArticleItem?)?.let {
                presenter.setDataFromItem(it)
            }
        }
    }

    override fun getLayoutResource(): Int = R.layout.fragment_paged

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ToolbarHelper.setTransparent(toolbar, appbarLayout)
        ToolbarHelper.setScrollFlag(toolbarLayout, AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED)
        ToolbarHelper.fixInsets(toolbar)
        ToolbarHelper.marqueeTitle(toolbar)

        toolbar.apply {
            //title = getString(R.string.fragment_title_release)
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

        toolbarImage.visibility = View.VISIBLE
        toolbarImage.setAspectRatio(0.5f)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbarImage.transitionName = transitionNameLocal
        }

        val scrimHelper = ScrimHelper(appbarLayout, toolbarLayout)
        scrimHelper.setScrimListener(object : ScrimHelper.ScrimListener {
            override fun onScrimChanged(scrim: Boolean) {
                if (scrim) {
                    toolbar.navigationIcon?.clearColorFilter()
                    toolbar.overflowIcon?.clearColorFilter()
                    toolbar.title = currentTitle
                } else {
                    toolbar.navigationIcon?.setColorFilter(currentColor, PorterDuff.Mode.SRC_ATOP)
                    toolbar.overflowIcon?.setColorFilter(currentColor, PorterDuff.Mode.SRC_ATOP)
                    toolbar.title = null
                }
            }
        })

        savedInstanceState?.let {
            pagerAdapter.webViewScrollPos = it.getInt(WEB_VIEW_SCROLL_Y, 0)
        }

        viewPager.adapter = pagerAdapter
    }

    override fun copyLink(url: String) {
        Utils.copyToClipBoard(url)
        Toast.makeText(context, "Ссылка скопирована", Toast.LENGTH_SHORT).show()
    }

    override fun share(text: String) {
        Utils.shareText(text)
    }

    override fun onResume() {
        super.onResume()
        pagerAdapter.onResume()
    }

    override fun onPause() {
        super.onPause()
        pagerAdapter.onPause()
    }

    override fun onDestroyView() {
        pagerAdapter.onDestroyView()
        toolbarHelperDisposable?.dispose()
        toolbarHelperDisposable = null
        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(WEB_VIEW_SCROLL_Y, pagerAdapter.getWebViewScroll())
        outState.putString(ArticleFragment.ARG_ID_NAME, presenter.articleIdCode)
        outState.putSerializable(ArticleFragment.ARG_ITEM, presenter.currentData)
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
        pagerAdapter.setRefreshing(refreshing)
    }

    override fun showArticle(article: ArticleItem) {
        currentTitle = article.title
        preShow(
                article.imageUrl,
                article.title,
                article.userNick,
                article.commentsCount,
                article.viewsCount
        )
        pagerAdapter.showArticle(article)
    }


    override fun preShow(imageUrl: String, title: String, nick: String, comments: Int, views: Int) {
        currentTitle = title
        ImageLoader.getInstance().displayImage(imageUrl, toolbarImage, object : SimpleImageLoadingListener() {
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
        pagerAdapter.preShow(imageUrl, title, nick, comments, views)
    }

    override fun showComments(comments: List<Comment>) {
        commentsAdapter.setComments(comments)
    }

    override fun insertMoreComments(comments: List<Comment>) {
        commentsAdapter.addComments(comments)
    }

    override fun setCommentsRefreshing(isRefreshing: Boolean) {
        pagerAdapter.setCommentsRefreshing(isRefreshing)
    }

    override fun setEndlessComments(enable: Boolean) {
        commentsAdapter.endless = enable
    }

    override fun onLoadMore() {
        presenter.loadMoreComments()
    }

    override fun onCommentSent() {
        hideSoftwareKeyboard()
        pagerAdapter.clearCommentField()
    }

    private inner class CustomPagerAdapter(
            private val commentsAdapter: CommentsAdapter
    ) : ViewStatePagerAdapter(), ExtendedWebView.JsLifeCycleListener {

        var webViewScrollPos = 0

        private val views = arrayOf(R.layout.fragment_article, R.layout.fragment_comments)

        private var localWebView: ExtendedWebView? = null
        private var localProgressSwitcher: ViewSwitcher? = null
        private var localCommentsRootLayout: ViewGroup? = null
        private val webViewCallCache = mutableListOf<Runnable>()


        override fun createView(container: ViewGroup, position: Int): View {
            Log.e("S_DEF_LOG", "instantiateItem $position")
            Log.e("S_DEF_LOG", "instantiateItem check $localWebView, $localProgressSwitcher")
            val inflater: LayoutInflater = LayoutInflater.from(container.context)
            val layout: ViewGroup = inflater.inflate(views[position], container, false) as ViewGroup
            container.addView(layout)
            if (position == 0) {
                createMain(layout)
                tryRunCache()
            } else if (position == 1) {
                createComments(layout)
            }
            return layout
        }

        override fun destroyView(container: ViewGroup, position: Int, view: View) {
            Log.e("S_DEF_LOG", "destroyItem $position")
            if (position == 0) {
                localWebView = null
                localProgressSwitcher = null
            }
        }

        override fun getCount(): Int = 2

        private fun createMain(layout: ViewGroup) {
            layout.run {
                localWebView = webView
                localWebView?.let {

                    it.webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                            val handled = presenter.onClickLink(url.orEmpty())
                            if (!handled) {
                                Utils.externalLink(url.orEmpty())
                            }
                            return true
                        }
                    }
                    it.setJsLifeCycleListener(this@CustomPagerAdapter)
                    val template = App.instance.articleTemplate
                    it.easyLoadData(Api.SITE_URL, template.generateOutput())
                    template.reset()
                }

                localProgressSwitcher = progressSwitcher
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

        fun setCommentsRefreshing(isRefreshing: Boolean) {
            localCommentsRootLayout?.commentsRefreshLayout?.isRefreshing = isRefreshing
        }


        fun showComments(comments: List<Comment>) {
            commentsAdapter.setComments(comments)
        }

        fun clearCommentField() {
            localCommentsRootLayout?.commentField?.text?.clear()
        }

        fun getWebViewScroll(): Int = localWebView?.scrollY ?: 0


        fun onResume() {
            localWebView?.onResume()
        }

        fun onPause() {
            localWebView?.onPause()
        }

        fun onDestroyView() {
            localWebView = null
            localProgressSwitcher = null
        }

        override fun onDomContentComplete(actions: ArrayList<String>) {

        }

        override fun onPageComplete(actions: ArrayList<String>) {
            localWebView?.syncWithJs {
                localWebView?.scrollTo(0, webViewScrollPos)
            }
        }

        fun setRefreshing(refreshing: Boolean) {
            webViewCallCache.add(Runnable {
                localProgressSwitcher?.displayedChild = if (refreshing) 1 else 0
            })
            tryRunCache()
        }

        fun showArticle(article: ArticleItem) {
            webViewCallCache.add(Runnable {
                localWebView?.evalJs("ViewModel.setText('content','${convert(article.content)}');")
            })
            tryRunCache()
        }

        fun preShow(imageUrl: String, title: String, nick: String, comments: Int, views: Int) {
            Log.e("S_DEF_LOG", "preshow $localWebView")
            Log.e("S_DEF_LOG", "preshow $imageUrl, $title, $nick, $comments, $views")
            webViewCallCache.add(Runnable {
                Log.e("S_DEF_LOG", "RUNNABLE preshow $imageUrl, $title, $nick, $comments, $views")
                localWebView?.evalJs("ViewModel.setText('title','${convert(title)}');")
                localWebView?.evalJs("ViewModel.setText('nick','${convert(nick)}');")
                localWebView?.evalJs("ViewModel.setText('comments_count','${convert(comments.toString())}');")
                localWebView?.evalJs("ViewModel.setText('views_count','${convert(views.toString())}');")
            })
            tryRunCache()
        }

        private fun convert(string: String): String {
            return Base64.encodeToString(string.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
        }

        private fun tryRunCache() {
            if (localWebView != null) {
                webViewCallCache.forEach { call ->
                    try {
                        localWebView?.syncWithJs(call)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
                webViewCallCache.clear()
            }
        }
    }
}
