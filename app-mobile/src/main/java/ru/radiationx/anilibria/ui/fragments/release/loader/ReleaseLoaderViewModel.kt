package ru.radiationx.anilibria.ui.fragments.release.loader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.data.api.releases.ReleaseRepository
import ru.radiationx.data.common.ReleaseAlias
import ru.radiationx.quill.QuillExtra
import ru.radiationx.shared.ktx.coRunCatching
import javax.inject.Inject

data class ReleaseLoaderExtra(
    val alias: ReleaseAlias
) : QuillExtra

class ReleaseLoaderViewModel @Inject constructor(
    private val extra: ReleaseLoaderExtra,
    private val router: Router,
    private val errorHandler: IErrorHandler,
    private val releaseRepository: ReleaseRepository
) : ViewModel() {

    fun loadRelease() {
        viewModelScope.launch {
            coRunCatching {
                releaseRepository.getReleaseByAlias(extra.alias)
            }.onSuccess { release ->
                router.replaceScreen(
                    Screens.ReleaseDetails(
                        id = release.id,
                        release = release
                    )
                )
            }.onFailure {
                errorHandler.handle(it)
                router.exit()
            }
        }
    }
}