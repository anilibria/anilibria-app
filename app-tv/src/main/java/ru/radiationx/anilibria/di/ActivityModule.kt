package ru.radiationx.anilibria.di

import android.content.Context
import androidx.fragment.app.FragmentActivity
import ru.radiationx.quill.QuillModule

//todo remove activity from DI
class ActivityModule(activity: FragmentActivity) : QuillModule() {

    init {
        instance<Context>(activity)
        instance<FragmentActivity>(activity)
        //instance { SystemUtils(activity) }
        //instance { CardsDataConverter(activity) }
        //instance { DetailDataConverter() }
        //instance { GradientBackgroundManager(activity) }
    }
}