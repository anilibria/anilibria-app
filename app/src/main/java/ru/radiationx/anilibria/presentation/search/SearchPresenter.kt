package ru.radiationx.anilibria.presentation.search

import android.os.Bundle
import android.util.Log
import com.arellomobile.mvp.InjectViewState
import ru.radiationx.anilibria.Screens
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.model.data.holders.ReleaseUpdateHolder
import ru.radiationx.anilibria.model.repository.ReleaseRepository
import ru.radiationx.anilibria.model.repository.SearchRepository
import ru.radiationx.anilibria.presentation.ErrorHandler
import ru.radiationx.anilibria.ui.fragments.release.details.ReleaseFragment
import ru.radiationx.anilibria.utils.mvp.BasePresenter
import ru.terrakok.cicerone.Router

@InjectViewState
class SearchPresenter(
        private val releaseRepository: ReleaseRepository,
        private val searchRepository: SearchRepository,
        private val router: Router,
        private val errorHandler: ErrorHandler,
        private val releaseUpdateHolder: ReleaseUpdateHolder
) : BasePresenter<SearchView>(router) {

    companion object {
        private const val START_PAGE = 1
    }

    private var currentPage = START_PAGE
    var currentGenre: String? = null
    var currentQuery: String? = null

    private val currentItems = mutableListOf<ReleaseItem>()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        Log.e("S_DEF_LOG", "onFirstViewAttach")
        loadGenres()
        observeGenres()
        loadReleases(START_PAGE)
        releaseUpdateHolder
                .observeEpisodes()
                .subscribe { data ->
                    val itemsNeedUpdate = mutableListOf<ReleaseItem>()
                    currentItems.forEach { item ->
                        data.firstOrNull { it.id == item.id }?.also { updItem ->
                            val isNew = item.torrentUpdate > updItem.lastOpenTimestamp || item.torrentUpdate > updItem.timestamp
                            Log.e("lalalupdata", "check pres ${item.id}, ${item.torrentUpdate} : ${updItem.id}, ${updItem.timestamp}, ${updItem.lastOpenTimestamp} : ${item.isNew}, $isNew")
                            if (item.isNew != isNew) {
                                item.isNew = isNew
                                itemsNeedUpdate.add(item)
                            }
                        }
                    }
                    viewState.updateReleases(itemsNeedUpdate)
                }
                .addToDisposable()
    }

    private fun isFirstPage(): Boolean {
        return currentPage == START_PAGE
    }

    fun fastSearch(query: String) {
        searchRepository
                .fastSearch(query)
                .subscribe({ searchItems ->
                    Log.d("S_DEF_LOG", "subscribe call show")
                    viewState.showFastItems(searchItems)
                }) {
                    errorHandler.handle(it)
                }
                .addToDisposable()
    }

    private fun loadGenres() {
        releaseRepository
                .getGenres()
                .subscribe({ }) {
                    errorHandler.handle(it)
                }
                .addToDisposable()
    }

    private fun observeGenres() {
        releaseRepository
                .observeGenres()
                .subscribe({
                    viewState.showGenres(it)
                }, {
                    errorHandler.handle(it)
                })
                .addToDisposable()
    }

    fun isEmpty(): Boolean = currentQuery.isNullOrEmpty() && currentGenre.isNullOrEmpty()

    private fun loadReleases(pageNum: Int) {
        Log.e("S_DEF_LOG", "loadReleases")

        /*if (isEmpty()) {
            viewState.setRefreshing(false)
            return
        }*/

        currentPage = pageNum
        if (isFirstPage()) {
            viewState.setRefreshing(true)
        }
        searchRepository
                .searchReleases(currentQuery.orEmpty(), currentGenre.orEmpty(), pageNum)
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({ releaseItems ->
                    viewState.setEndless(!releaseItems.isEnd())
                    showData(releaseItems.data)
                }) {
                    showData(emptyList())
                    errorHandler.handle(it)
                }
                .addToDisposable()
    }

    private fun showData(data: List<ReleaseItem>) {
        if (isFirstPage()) {
            currentItems.clear()
            currentItems.addAll(data)
            viewState.showReleases(data)
        } else {
            currentItems.addAll(data)
            viewState.insertMore(data)
        }
    }

    fun refreshReleases() {
        loadReleases(START_PAGE)
    }

    fun loadMore() {
        loadReleases(currentPage + 1)
    }

    fun onItemClick(item: ReleaseItem) {
        val args = Bundle()
        args.putInt(ReleaseFragment.ARG_ID, item.id)
        args.putString(ReleaseFragment.ARG_ID_CODE, item.idName)
        args.putSerializable(ReleaseFragment.ARG_ITEM, item)
        router.navigateTo(Screens.RELEASE_DETAILS, args)
    }

    fun onItemLongClick(item: ReleaseItem): Boolean {
        return false
    }
}
