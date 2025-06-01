package ru.radiationx.anilibria.screen.update

import ru.radiationx.data.app.updater.models.UpdateData
import ru.radiationx.shared.ktx.EventFlow
import javax.inject.Inject

class UpdateController @Inject constructor() {

    val downloadAction = EventFlow<UpdateData.UpdateLink>()
}