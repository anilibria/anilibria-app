package ru.radiationx.anilibria.screen.update

import com.jakewharton.rxrelay2.PublishRelay
import ru.radiationx.data.entity.app.updater.UpdateData
import toothpick.InjectConstructor

@InjectConstructor
class UpdateController {

    val downloadAction = PublishRelay.create<UpdateData.UpdateLink>()
}