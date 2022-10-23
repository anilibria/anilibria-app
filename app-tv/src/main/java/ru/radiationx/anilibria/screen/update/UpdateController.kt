package ru.radiationx.anilibria.screen.update

import kotlinx.coroutines.flow.MutableSharedFlow
import ru.radiationx.data.entity.app.updater.UpdateData
import toothpick.InjectConstructor

@InjectConstructor
class UpdateController {

    val downloadAction = MutableSharedFlow<UpdateData.UpdateLink>()
}