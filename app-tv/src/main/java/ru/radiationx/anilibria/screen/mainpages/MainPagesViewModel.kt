package ru.radiationx.anilibria.screen.mainpages

import androidx.lifecycle.MutableLiveData
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.anilibria.screen.SearchScreen
import ru.radiationx.anilibria.screen.SuggestionsScreen
import ru.radiationx.anilibria.screen.UpdateScreen
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.repository.CheckerRepository
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

@InjectConstructor
class MainPagesViewModel(
    private val checkerRepository: CheckerRepository,
    private val buildConfig: SharedBuildConfig,
    private val guidedRouter: GuidedRouter,
    private val router: Router
) : LifecycleViewModel() {

    val hasUpdatesData = MutableLiveData<Boolean>()

    override fun onCreate() {
        super.onCreate()

        checkerRepository
            .checkUpdate(buildConfig.versionCode, true)
            .lifeSubscribe({
                hasUpdatesData.value = it.code > buildConfig.versionCode
            }, {
                it.printStackTrace()
            })
    }

    fun onAppUpdateClick() {
        router.navigateTo(UpdateScreen())
    }

    fun onCatalogClick() {
        router.navigateTo(SearchScreen())
    }

    fun onSearchClick() {
        router.navigateTo(SuggestionsScreen())
    }
}