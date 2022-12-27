package ru.radiationx.anilibria.screen.update

import ru.radiationx.data.entity.domain.updater.UpdateData
import ru.radiationx.shared.ktx.EventFlow
import toothpick.InjectConstructor

@InjectConstructor
class UpdateController {

    val downloadAction = EventFlow<UpdateData.UpdateLink>()
}