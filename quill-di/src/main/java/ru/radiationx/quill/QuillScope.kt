package ru.radiationx.quill

import toothpick.Scope
import toothpick.Toothpick
import kotlin.reflect.KClass

class QuillScope(
    private val tpScope: Scope,
) {

    val name: Any = tpScope.name

    fun openSubScope(): QuillScope {
        val tpSubScope = tpScope.openSubScope(Quill.generateScopeName())
        return QuillScope(tpSubScope)
    }

    fun close() {
        Toothpick.closeScope(tpScope.name)
    }

    fun <T : Any> get(clazz: KClass<T>, qualifier: KClass<out Annotation>? = null): T {
        return qualifier
            ?.let { tpScope.getInstance(clazz.java, it.java.canonicalName) }
            ?: tpScope.getInstance(clazz.java)
    }

    fun installModules(vararg modules: QuillModule) {
        tpScope.installModules(*modules.map { it.tpModule }.toTypedArray())
    }
}