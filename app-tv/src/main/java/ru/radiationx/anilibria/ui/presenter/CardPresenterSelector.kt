package ru.radiationx.anilibria.ui.presenter

import androidx.leanback.widget.Presenter
import androidx.leanback.widget.PresenterSelector
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.common.LinkCard
import ru.radiationx.anilibria.common.LoadingCard

class CardPresenterSelector : PresenterSelector() {

    private val presentersMap = mutableMapOf<Class<*>, Presenter>()

    override fun getPresenter(item: Any): Presenter {
        val presenter = presentersMap[item::class.java]
        if (presenter != null) {
            return presenter
        }
        presentersMap[item::class.java] = when (item) {
            is LibriaCard -> LibriaCardPresenter()
            is LinkCard -> LinkCardPresenter()
            is LoadingCard -> LoadingCardPresenter()
            else -> throw RuntimeException("No presenter for $item")
        }
        return presentersMap.getValue(item::class.java)
    }
}