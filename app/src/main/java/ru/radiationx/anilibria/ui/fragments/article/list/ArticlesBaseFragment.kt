package ru.radiationx.anilibria.ui.fragments.article.list

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import kotlinx.android.synthetic.main.fragment_releases.*
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.article.ArticleItem
import ru.radiationx.anilibria.entity.app.vital.VitalItem
import ru.radiationx.anilibria.presentation.article.list.ArticlesPresenter
import ru.radiationx.anilibria.presentation.article.list.ArticlesView
import ru.radiationx.anilibria.ui.common.BackButtonListener
import ru.radiationx.anilibria.ui.common.RouterProvider
import ru.radiationx.anilibria.ui.fragments.SharedProvider
import ru.radiationx.anilibria.ui.widgets.UniversalItemDecoration
import ru.radiationx.anilibria.utils.Utils
import ru.terrakok.cicerone.Router

/**
 * Created by radiationx on 16.12.17.
 */
abstract class ArticlesBaseFragment : MvpAppCompatFragment(), BackButtonListener, ArticlesView, SharedProvider, ArticlesAdapter.ItemListener {

    companion object {
        const val ARG_CATEGORY = "category"
    }

    abstract val spinnerItems: List<Pair<String, String>>

    open var category = ""

    private val adapter: ArticlesAdapter by lazy { ArticlesAdapter(this) }
    lateinit var router: Router

    @InjectPresenter
    lateinit var presenter: ArticlesPresenter

    @ProvidePresenter
    fun provideArticlesPresenter(): ArticlesPresenter = ArticlesPresenter(
            App.injections.articleRepository,
            App.injections.vitalRepository,
            (parentFragment as RouterProvider).getRouter(),
            App.injections.errorHandler
    )

    fun onSelectCategory(category: String) {
        Log.e("lalala", "onSelectCategory $category, ${this::presenter.isInitialized}, $this")
        this.category = category
        if (this::presenter.isInitialized) {
            presenter.loadCategory(category)
        }
    }

    override var sharedViewLocal: View? = null

    override fun getSharedView(): View? {
        val sharedView = sharedViewLocal
        sharedViewLocal = null
        return sharedView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("lalala", "onCreate $this")
        category = savedInstanceState?.getString(ARG_CATEGORY) ?: category
        presenter.category = category
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val newView: View? = inflater.inflate(R.layout.fragment_releases, container, false)
        return newView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e("S_DEF_LOG", "TEST onViewCreated " + this)
        refreshLayout.setOnRefreshListener { presenter.refresh() }

        recyclerView.apply {
            adapter = this@ArticlesBaseFragment.adapter
            layoutManager = LinearLayoutManager(recyclerView.context)
            addItemDecoration(UniversalItemDecoration()
                    .fullWidth(true)
                    .spacingDp(8f)
            )
        }


        /*toolbar.apply {
            title = getString(R.string.fragment_title_news)
        }*/

        /*spinner.apply {
            spinnerContainer.visibility = View.VISIBLE

            adapter = ArrayAdapter<String>(
                    spinner.context,
                    R.layout.item_view_spinner,
                    spinnerItems.map { it.second }
            )
            (adapter as ArrayAdapter<*>).setDropDownViewResource(R.layout.item_view_spinner_dropdown)

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    Log.e("S_DEF_LOG", "TEST onItemSelected " + p2)
                    presenter.loadCategory(spinnerItems[p2].first)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
        }*/
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(ARG_CATEGORY, category)
    }

    override fun onBackPressed(): Boolean {
        presenter.onBackPressed()
        return true
    }

    override fun showVitalItems(vital: List<VitalItem>) {
        adapter.setVitals(vital)
    }

    override fun setEndless(enable: Boolean) {
        adapter.endless = enable
    }

    override fun showArticles(articles: List<ArticleItem>) {
        adapter.bindItems(articles)
    }

    override fun insertMore(articles: List<ArticleItem>) {
        adapter.insertMore(articles)
    }

    override fun onLoadMore() {
        presenter.loadMore()
    }

    override fun setRefreshing(refreshing: Boolean) {
        refreshLayout.isRefreshing = refreshing
    }

    override fun onItemClick(position: Int, view: View) {
        sharedViewLocal = view
    }

    override fun onItemClick(item: ArticleItem, position: Int) {
        presenter.onItemClick(item)
    }

    override fun onItemLongClick(item: ArticleItem): Boolean {
        context?.let {
            val titles = arrayOf("Копировать ссылку", "Поделиться")
            AlertDialog.Builder(it)
                    .setItems(titles, { dialog, which ->
                        when (which) {
                            0 -> {
                                Utils.copyToClipBoard(item.url)
                                Toast.makeText(it, "Ссылка скопирована", Toast.LENGTH_SHORT).show()
                            }
                            1 -> Utils.shareText(item.url)
                        }
                    })
                    .show()
        }
        return false
    }

    /*override fun onItemLongClick(item: ArticleItem): Boolean {
        return presenter.onItemLongClick(item)
    }*/
}
