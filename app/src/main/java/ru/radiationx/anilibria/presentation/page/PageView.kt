package ru.radiationx.anilibria.presentation.page

import ru.radiationx.anilibria.entity.app.page.PageLibria
import ru.radiationx.anilibria.utils.mvp.IBaseView

/**
 * Created by radiationx on 13.01.18.
 */
interface PageView : IBaseView {
    fun showPage(page: PageLibria)
}