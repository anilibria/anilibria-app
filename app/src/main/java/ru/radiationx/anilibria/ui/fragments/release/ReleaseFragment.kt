package ru.radiationx.anilibria.ui.fragments.release

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast

import com.arellomobile.mvp.presenter.InjectPresenter
import kotlinx.android.synthetic.main.fragment_main_base.*
import kotlinx.android.synthetic.main.fragment_release.*
import ru.radiationx.anilibria.App

import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.Screens
import ru.radiationx.anilibria.data.Client
import ru.radiationx.anilibria.data.api.releases.ReleaseItem
import ru.radiationx.anilibria.ui.activities.MainActivity
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.search.SearchFragment
import ru.radiationx.anilibria.utils.Utils

/* Created by radiationx on 16.11.17. */

open class ReleaseFragment : BaseFragment(), ReleaseView, ReleaseAdapter.ReleaseListener {
    override val layoutRes: Int = R.layout.fragment_release

    companion object {
        const val ARG_ID: String = "release_id"
        const val ARG_ITEM: String = "release_item"
    }


    private var adapter: ReleaseAdapter = ReleaseAdapter()

    @InjectPresenter
    lateinit var presenter: ReleasePresenter

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


    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter.setReleaseListener(this)
        recyclerView.apply {
            setHasFixedSize(true)
            adapter = this@ReleaseFragment.adapter
            layoutManager = LinearLayoutManager(recyclerView.context)
        }

        toolbar.apply {
            title = getString(R.string.fragment_title_release)
            setNavigationOnClickListener({
                App.get().router.backTo(Screens.RELEASES_LIST)
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

    override fun onClickSd(url: String) {
        Utils.externalLink(url)
    }

    override fun onClickHd(url: String) {
        Utils.externalLink(url)
    }

    override fun onClickTorrent(url: String) {
        presenter.onTorrentClick()
    }

    override fun onClickWatchAll() {
        presenter.onWatchAllClick()
    }

    override fun onClickTag(text: String) {
        val args: Bundle = Bundle().apply {
            putString(SearchFragment.GENRE, text)
        }
        App.get().router.navigateTo(Screens.RELEASES_SEARCH, args)
    }

    override fun watchEpisodes(episodes: List<ReleaseItem.Episode>) {
        Toast.makeText(context, "Временно не поддерживается", Toast.LENGTH_SHORT).show()
    }

    override fun watchMoonwalk(link: String) {
        Utils.externalLink(link)
    }
}
