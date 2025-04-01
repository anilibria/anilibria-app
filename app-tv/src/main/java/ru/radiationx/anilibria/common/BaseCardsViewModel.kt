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

    /** Итоговые карточки для показа (LibriaCard, LinkCard, LoadingCard и т.д.) */
    val cardsData = MutableStateFlow<List<CardItem>>(emptyList())

    /** Заголовок ряда. */
    val rowTitle = MutableStateFlow("")

    /** С какой страницы начинаем загрузку. Обычно 1. */
    protected open val firstPage = 1

    /** Загружать ли автоматически при первом создании (onColdCreate). */
    protected open val loadOnCreate = true

    /**
     * Показывать ли «LoadingCard»/«Progress» при обновлении (onRefreshClick)?
     * Если `false`, то при обновлении карточки «Loading» не будет.
     */
    protected open val progressOnRefresh = true

    /**
     * Нужно ли предотвращать «clear» списка при обновлении?
     * Если `true`, при обновлении мы не очищаем старые карточки, а только добавляем новые.
     */
    protected open val preventClearOnRefresh = false

    /** Текст, который хотим изначально. */
    open val defaultTitle: String = "Cards"

    /**
     * Карточка для «загрузить ещё».
     * Если она нужна — её вставляют в конец списка, когда есть следующая страница.
     */
    protected open val loadMoreCard = LinkCard("Загрузить еще")

    /** Карточка для отображения «в процессе загрузки». */
    protected open val loadingCard = LoadingCard("Загрузка данных")

    /** Текущий список LibriaCard (успешно загруженные). */
    private val currentCards = mutableListOf<LibriaCard>()

    /** Текущая страница (если есть пагинация). */
    private var currentPage = -1

    /** Job для отмены/предотвращения параллельных запросов. */
    private var requestJob: Job? = null

    override fun onColdCreate() {
        super.onColdCreate()
        rowTitle.value = defaultTitle
        if (loadOnCreate) {
            onRefreshClick()
        }
    }

    /** Вызывается, когда нажали на «LinkCard(Загрузить ещё)». */
    open fun onLinkCardClick() {
        onLinkCardBind()
    }

    /** Можно переопределить, если нужна особая логика при биндинге/фокусе LinkCard. */
    open fun onLinkCardBind() {
        loadPage(currentPage + 1)
    }

    /** Нажали «обновить» (обычно перезагрузить c первой страницы). */
    open fun onRefreshClick() {
        loadPage(firstPage)
    }

    /** Нажали на «LoadingCard», если она была в состоянии ошибки. */
    open fun onLoadingCardClick() {
        loadPage(currentPage)
    }

    /** При клике по обычной карточке (LibriaCard). Переопределяйте в наследниках. */
    open fun onLibriaCardClick(card: LibriaCard) {}

    /**
     * Нужно реализовать в наследниках:
     * какую именно порцию данных грузить при запросе конкретной страницы.
     */
    protected abstract suspend fun getLoader(requestPage: Int): List<LibriaCard>

    /**
     * Когда нужно показать кнопку «Загрузить ещё».
     * По умолчанию проверяем, что в ответе >= 10 элементов (условно).
     * сейчас это бесконечная загрузка по кругу - это фича, не трогать
     */
    protected open fun hasMoreCards(
        newCards: List<LibriaCard>,
        allCards: List<LibriaCard>,
    ): Boolean {
        return newCards.size >= 10
    }

    /**
     * Нужно ли обновлять/перезаписывать текущие карточки при обновлении.
     * Если `preventClearOnRefresh = true`, мы сравниваем id и решаем, менять ли начало списка.
     */
    protected open fun needsModify(
        newCards: List<LibriaCard>,
        allCards: List<LibriaCard>,
    ): Boolean {
        if (!preventClearOnRefresh) return true
        val oldFirstIds = allCards.take(newCards.size).map { it.getId() }.toSet()
        val newIds = newCards.map { it.getId() }.toSet()
        return oldFirstIds != newIds
    }

    /** Карточка-ошибка, если запрос упал. Можно переопределить вид текста и т.п. */
    protected open fun getErrorCard(error: Throwable): LoadingCard {
        return LoadingCard(
            title = "Повторить загрузку",
            description = "Произошла ошибка: ${error.message}",
            isError = true
        )
    }

    /** Главный метод для загрузки (первая или следующая страница). */
    private fun loadPage(requestPage: Int) {
        if (requestJob?.isActive == true) return
        requestJob = viewModelScope.launch {
            // Показываем «loadingCard», если (не первая страница) или при принуд. прогрессе
            if (requestPage != firstPage || progressOnRefresh) {
                cardsData.value = currentCards + loadingCard
            }
            coRunCatching {
                withContext(Dispatchers.IO) { getLoader(requestPage) }
            }.onSuccess { newCards ->
                val isFirstPage = requestPage <= 1
                val allowModify = if (isFirstPage) {
                    needsModify(newCards, currentCards)
                } else true

                if (isFirstPage && allowModify) {
                    currentCards.clear()
                }
                if (allowModify) {
                    currentPage = requestPage
                    currentCards.addAll(newCards)
                }
                // Если ещё есть страницы — добавим linkCard, иначе нет
                cardsData.value = if (hasMoreCards(newCards, currentCards)) {
                    currentCards + loadMoreCard
                } else {
                    currentCards
                }
            }.onFailure { error ->
                Timber.e(error)
                cardsData.value = currentCards + getErrorCard(error)
            }
        }
    }
}
