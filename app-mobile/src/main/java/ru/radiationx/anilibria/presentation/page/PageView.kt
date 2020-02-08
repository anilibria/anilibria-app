package ru.radiationx.anilibria.presentation.page

import ru.radiationx.data.entity.app.page.PageLibria
import ru.radiationx.anilibria.presentation.common.IBaseView

/**
 * Created by radiationx on 13.01.18.
 */
interface PageView : IBaseView {
    fun showPage(page: PageLibria)
}