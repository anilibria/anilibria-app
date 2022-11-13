package ru.radiationx.shared_app.imageloader

import ru.radiationx.shared_app.di.DI

internal object LibriaImageLoaderRoot {

    fun getImpl(): LibriaImageLoader {
        return DI.get(LibriaImageLoader::class.java)
    }
}