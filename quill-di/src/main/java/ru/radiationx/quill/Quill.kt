package ru.radiationx.quill

import toothpick.Toothpick
import java.util.*


object Quill {

    private val scope by lazy(LazyThreadSafetyMode.NONE) {
        QuillScope(Toothpick.openRootScope())
    }

    fun getRootScope(): QuillScope {
        return scope
    }

    fun generateScopeName(): UUID {
        return UUID.randomUUID()
    }

}