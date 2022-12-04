package ru.radiationx.anilibria.ui.fragments.release.details

import ru.radiationx.quill.QuillModule

class ReleaseModule : QuillModule() {
    init {
        instance {
            ReleaseCommentsNotifier()
        }
    }
}