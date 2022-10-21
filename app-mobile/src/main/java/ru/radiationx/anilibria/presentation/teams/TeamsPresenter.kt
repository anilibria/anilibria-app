package ru.radiationx.anilibria.presentation.teams

import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.data.entity.domain.team.Teams
import ru.radiationx.data.repository.TeamsRepository
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

@InjectConstructor
class TeamsPresenter(
    router: Router,
    private val repository: TeamsRepository,
    private val errorHandler: IErrorHandler
) : BasePresenter<TeamsView>(router) {

    private var currentData: Teams? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        repository
            .getTeams()
            .doOnSubscribe { viewState.setLoading(true) }
            .doFinally { viewState.setLoading(false) }
            .subscribe({
                currentData = it
                viewState.showData(it)
            }, {
                errorHandler.handle(it)
            })
            .addToDisposable()
    }
}