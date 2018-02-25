package ru.radiationx.anilibria.ui.fragments.article.list

import android.os.Bundle
import ru.radiationx.anilibria.model.data.remote.Api

/**
 * Created by radiationx on 16.12.17.
 */
class BlogsFragment : ArticlesFragment() {

    override val spinnerItems = listOf(
            "blog" to "Все блоги",
            "audioblog_lln" to "Новости (ЛЛН)",
            "sharon" to "Шаровые диалоги",
            "newblogofitashi" to "Блоги Itashi",
            "silvologia" to "Блоги Silv",
            "animeteapublic" to "Чайный домик"/*,
            "j_r" to "Джей Райм"*/
    )

    override var category = Api.CATEGORY_BLOGS
}
