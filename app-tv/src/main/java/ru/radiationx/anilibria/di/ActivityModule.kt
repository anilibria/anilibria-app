package ru.radiationx.anilibria.di

import android.content.Context
import androidx.fragment.app.FragmentActivity
import ru.radiationx.anilibria.common.CardsDataConverter
import ru.radiationx.anilibria.common.DetailDataConverter
import ru.radiationx.anilibria.common.GradientBackgroundManager
import ru.radiationx.quill.QuillModule
import ru.radiationx.shared_app.common.SystemUtils

//todo remove activity from DI
class ActivityModule(activity: FragmentActivity) : QuillModule() {

    init {
        instance<Context> { activity }
        instance { SystemUtils(activity) }
        instance { CardsDataConverter(activity) }
        instance { DetailDataConverter(activity) }
        instance { GradientBackgroundManager(activity) }
    }
}