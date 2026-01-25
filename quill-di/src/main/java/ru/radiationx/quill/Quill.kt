package ru.radiationx.quill

import toothpick.Toothpick
import toothpick.configuration.Configuration
import java.util.UUID


object Quill {

    private val scope by lazy {
        Toothpick.setConfiguration(Configuration.forProduction())
        QuillScope(Toothpick.openRootScope())
    }

    fun getRootScope(): QuillScope {
        return scope
    }

    fun generateScopeName(): UUID {
        return UUID.randomUUID()
    }

}