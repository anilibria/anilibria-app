package ru.radiationx.anilibria.ui.fragments.release

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import kotlinx.android.synthetic.main.fragment_main_base.*
import kotlinx.android.synthetic.main.fragment_release.*
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.data.api.models.ReleaseItem
import ru.radiationx.anilibria.ui.activities.MyPlayerActivity
import ru.radiationx.anilibria.ui.common.RouterProvider
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.utils.Utils


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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter.setReleaseListener(releaseListener)
        recyclerView.apply {
            setHasFixedSize(true)
            adapter = this@ReleaseFragment.adapter
            layoutManager = LinearLayoutManager(recyclerView.context)
        }

        toolbar.apply {
            title = getString(R.string.fragment_title_release)
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

    override fun showRelease(release: ReleaseItem) {
        toolbar.title = String.format("%s / %s", release.title, release.originalTitle)
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
        appbar_layout.setExpanded(false, false)
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
