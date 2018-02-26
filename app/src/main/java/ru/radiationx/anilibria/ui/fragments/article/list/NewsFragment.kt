package ru.radiationx.anilibria.ui.fragments.article.list

/**
 * Created by radiationx on 26.02.18.
 */
class NewsFragment : ArticlesBaseFragment() {
    override val spinnerItems = listOf(
            "" to "Главная",
            "novosti" to "Новости"
    )
}