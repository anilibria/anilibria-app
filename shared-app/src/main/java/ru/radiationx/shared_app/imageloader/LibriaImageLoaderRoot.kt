package ru.radiationx.shared_app.imageloader

import ru.radiationx.quill.Quill

internal object LibriaImageLoaderRoot {

    fun getImpl(): LibriaImageLoader {
        return Quill.getRootScope().get(LibriaImageLoader::class)
    }
}