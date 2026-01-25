package ru.radiationx.anilibria.screen.update

import ru.radiationx.data.entity.domain.updater.UpdateData
import ru.radiationx.shared.ktx.EventFlow
import javax.inject.Inject

class UpdateController @Inject constructor() {

    val downloadAction = EventFlow<UpdateData.UpdateLink>()
}