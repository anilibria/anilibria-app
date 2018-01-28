package ru.radiationx.anilibria.presentation.checker

import ru.radiationx.anilibria.entity.app.updater.UpdateData
import ru.radiationx.anilibria.utils.mvp.IBaseView

/**
 * Created by radiationx on 28.01.18.
 */
interface CheckerView : IBaseView {

    fun showUpdateData(update: UpdateData)
}