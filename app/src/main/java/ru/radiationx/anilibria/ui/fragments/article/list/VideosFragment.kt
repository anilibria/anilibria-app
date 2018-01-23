package ru.radiationx.anilibria.ui.fragments.article.list

import android.os.Bundle
import ru.radiationx.anilibria.model.data.remote.Api

/**
 * Created by radiationx on 16.12.17.
 */
class VideosFragment : ArticlesFragment() {

    override val spinnerItems = listOf(
            "video" to "Все видео",
            "rap" to "RAP-лбзоры",
            "fisheyeplacebo" to "Fisheye Placebo",
            "anons" to "Анонсы и ТОП-10",
            "amv" to "AMV",
            "tkj" to "Тот, кто живёт"
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.category = Api.CATEGORY_VIDEOS
    }
}
