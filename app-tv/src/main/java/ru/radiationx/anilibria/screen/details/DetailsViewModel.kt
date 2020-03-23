package ru.radiationx.anilibria.screen.details

import ru.radiationx.anilibria.common.BaseRowsViewModel
import ru.radiationx.data.interactors.ReleaseInteractor
import toothpick.InjectConstructor

@InjectConstructor
class DetailsViewModel(
    private val releaseInteractor: ReleaseInteractor
) : BaseRowsViewModel() {

    companion object {
        private val linkPattern = Regex("\\/release\\/([^.]+?)\\.html")

        const val RELEASE_ROW_ID = 1L
        const val RELATED_ROW_ID = 2L
        const val RECOMMENDS_ROW_ID = 3L

        fun getReleasesFromDesc(description: String): List<String> {
            return linkPattern.findAll(description).map { it.groupValues[1] }.toList()
        }
    }

    var releaseId: Int = -1

    override val rowIds: List<Long> = listOf(RELEASE_ROW_ID, RELATED_ROW_ID, RECOMMENDS_ROW_ID)

    override val availableRows: MutableList<Long> = mutableListOf(RELEASE_ROW_ID, RELATED_ROW_ID, RECOMMENDS_ROW_ID)

    override fun onCreate() {
        super.onCreate()

        releaseInteractor
            .loadRelease(releaseId)
            .lifeSubscribe { }


        val release = releaseInteractor.getFull(releaseId) ?: releaseInteractor.getItem(releaseId)
        val releaseCodes = getReleasesFromDesc(release?.description.orEmpty())
        updateAvailableRow(RELATED_ROW_ID, releaseCodes.isNotEmpty())
    }
}