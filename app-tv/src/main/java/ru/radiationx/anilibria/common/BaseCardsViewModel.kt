package ru.radiationx.anilibria.common

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.shared.ktx.coRunCatching
import timber.log.Timber

abstract class BaseCardsViewModel : LifecycleViewModel() {

    val cardsData = MutableStateFlow<List<CardItem>>(emptyList())
    val rowTitle = MutableStateFlow("")

    protected open val firstPage = 1
    protected open val loadOnCreate = true
    protected open val progressOnRefresh = true
    protected open val preventClearOnRefresh = false
    open val defaultTitle = "Cards"

    protected open val loadMoreCard = LinkCard("Загрузить еще")
    protected open val loadingCard = LoadingCard("Загрузка данных")

    private val currentCards = mutableListOf<LibriaCard>()
    private var currentPage = -1

    private var requestJob: Job? = null

    override fun onColdCreate() {
        super.onColdCreate()
        rowTitle.value = defaultTitle
        if (loadOnCreate) {
            onRefreshClick()
        }
    }

    open fun onLinkCardClick() {
        onLinkCardBind()
    }

    open fun onLinkCardBind() {
        loadPage(currentPage + 1)
    }

    open fun onRefreshClick() {
        loadPage(firstPage)
    }

    open fun onLoadingCardClick() {
        loadPage(currentPage)
    }

    open fun onLibriaCardClick(card: LibriaCard) {}

    protected abstract suspend fun getLoader(requestPage: Int): List<LibriaCard>

    protected open fun hasMoreCards(
        newCards: List<LibriaCard>,
        allCards: List<LibriaCard>,
    ): Boolean {
        return newCards.size >= 10
    }

    protected open fun needsModify(
        newCards: List<LibriaCard>,
        allCards: List<LibriaCard>,
    ): Boolean {
        if (!preventClearOnRefresh) return true
        val oldFirstIds = allCards.take(newCards.size).map { it.getId() }.toSet()
        val newIds = newCards.map { it.getId() }.toSet()
        return oldFirstIds != newIds
    }

    protected open fun getErrorCard(error: Throwable) = LoadingCard(
        "Повторить загрузку",
        "Произошла ошибка ${error.message}",
        isError = true
    )

    private fun loadPage(requestPage: Int) {
        if (requestJob?.isActive == true) {
            return
        }
        requestJob?.cancel()
        requestJob = viewModelScope.launch {
            if (requestPage != firstPage || progressOnRefresh) {
                cardsData.value = currentCards + loadingCard
            }
            coRunCatching {
                withContext(Dispatchers.IO) {
                    getLoader(requestPage)
                }
            }.onSuccess { newCards ->
                currentPage = requestPage
                val isFirstPage = requestPage <= 1
                val needsModify = if (isFirstPage) {
                    needsModify(newCards, currentCards)
                } else {
                    true
                }
                if (isFirstPage && needsModify) {
                    currentCards.clear()
                }
                if (needsModify) {
                    currentCards.addAll(newCards)
                }

                if (hasMoreCards(newCards, currentCards)) {
                    cardsData.value = currentCards + loadMoreCard
                } else {
                    cardsData.value = currentCards
                }
            }.onFailure {
                Timber.e(it)
                cardsData.value = currentCards + getErrorCard(it)
            }
        }
    }

}