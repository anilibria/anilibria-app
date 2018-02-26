package ru.radiationx.anilibria.ui.fragments.article.list

import ru.radiationx.anilibria.model.data.remote.Api

/**
 * Created by radiationx on 16.12.17.
 */
class VideosFragment : ArticlesBaseFragment() {

    override val spinnerItems = listOf(
            "video" to "Все видео",
            "rap" to "RAP-обзоры",
            "fisheyeplacebo" to "Fisheye Placebo",
            "anons" to "Анонсы и ТОП-10",
            "amv" to "AMV",
            "tkj" to "Тот, кто живёт"
    )

    override var category = Api.CATEGORY_VIDEOS

}
