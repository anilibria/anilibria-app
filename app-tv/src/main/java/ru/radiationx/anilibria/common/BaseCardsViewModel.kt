package ru.radiationx.anilibria.common

import android.util.Log
import androidx.lifecycle.MutableLiveData
import io.reactivex.Single
import io.reactivex.disposables.Disposables
import ru.radiationx.anilibria.screen.LifecycleViewModel

abstract class BaseCardsViewModel : LifecycleViewModel() {

    val cardsData = MutableLiveData<List<Any>>()
    val rowTitle = MutableLiveData<String>()

    protected open val firstPage = 1
    protected open val perPage = 20
    protected open val loadOnCreate = true
    open val defaultTitle = "Cards"

    protected open val loadMoreCard = LinkCard("Загрузить еще")
    protected open val loadingCard = LoadingCard("Загрузка данных")

    protected val currentCards = mutableListOf<LibriaCard>()
    protected var currentPage = -1
        private set
    private var requestDisposable = Disposables.disposed()

    override fun onColdCreate() {
        super.onColdCreate()
        rowTitle.value = defaultTitle
    }

    override fun onCreate() {
        super.onCreate()
        if (loadOnCreate) {
            onRefreshClick()
        }
    }

    open fun onLinkCardClick() {
        currentPage++
        loadPage(currentPage)
    }

    open fun onRefreshClick() {
        currentPage = firstPage
        loadPage()
    }

    open fun onLoadingCardClick() {
        loadPage()
    }

    open fun onLibriaCardClick(card: LibriaCard) {}

    protected abstract fun getLoader(requestPage: Int): Single<List<LibriaCard>>

    protected open fun hasMoreCards(newCards: List<LibriaCard>, allCards: List<LibriaCard>): Boolean =
        newCards.size >= 10 && newCards.isNotEmpty()

    protected open fun getErrorCard(error: Throwable) = LoadingCard(
        "Повторить загрузку",
        "Произошла ошибка ${error.message}",
        isError = true
    )

    private fun loadPage(requestPage: Int = currentPage) {

        Log.e("lalala", "request load page $requestPage")
        cardsData.value = currentCards + loadingCard

        requestDisposable.dispose()
        requestDisposable = getLoader(requestPage)
            .lifeSubscribe({ newCards ->
                Log.e("lalala", "loaded page $requestPage")
                if (currentPage <= 1) {
                    currentCards.clear()
                }
                currentCards.addAll(newCards)

                if (hasMoreCards(newCards, currentCards)) {
                    cardsData.value = currentCards + loadMoreCard
                } else {
                    cardsData.value = currentCards
                }
            }, {
                it.printStackTrace()
                cardsData.value = currentCards + getErrorCard(it)
            })
    }

}