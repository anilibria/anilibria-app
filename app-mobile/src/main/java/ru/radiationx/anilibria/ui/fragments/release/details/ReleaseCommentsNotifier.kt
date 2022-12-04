package ru.radiationx.anilibria.ui.fragments.release.details

import ru.radiationx.shared.ktx.EventFlow

class ReleaseCommentsNotifier {

    private val events = EventFlow<Unit>()

    fun observe() = events.observe()

    fun requireOpen(){
        events.set(Unit)
    }
}