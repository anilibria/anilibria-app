package ru.radiationx.anilibria.di

import android.content.Context
import androidx.fragment.app.FragmentActivity
import ru.radiationx.anilibria.DetailDataConverter
import ru.radiationx.anilibria.common.CardsDataConverter
import ru.radiationx.anilibria.common.GradientBackgroundManager
import ru.radiationx.shared_app.common.SystemUtils
import toothpick.config.Module

class ActivityModule(activity: FragmentActivity) : Module() {

    init {
        bind(Context::class.java).toInstance(activity)
        bind(SystemUtils::class.java).toInstance(SystemUtils(activity))
        bind(CardsDataConverter::class.java).toInstance(CardsDataConverter(activity))
        bind(DetailDataConverter::class.java).toInstance(DetailDataConverter(activity))
    }
}