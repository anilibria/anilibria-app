package ru.radiationx.anilibria.di

import ru.radiationx.anilibria.utils.DimensionsProvider
import ru.radiationx.quill.QuillModule

class DimensionsModule : QuillModule() {
    init {
        single<DimensionsProvider>()
    }
}