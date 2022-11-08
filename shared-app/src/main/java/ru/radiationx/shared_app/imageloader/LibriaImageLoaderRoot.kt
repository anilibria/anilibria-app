package ru.radiationx.shared_app.imageloader

object LibriaImageLoaderRoot {

    private var currentLoader: LibriaImageLoader? = null

    fun setImpl(loader: LibriaImageLoader) {
        currentLoader = loader
    }

    fun getImpl(): LibriaImageLoader {
        val loader = currentLoader
        return requireNotNull(loader) {
            "Set implementation first"
        }
    }
}