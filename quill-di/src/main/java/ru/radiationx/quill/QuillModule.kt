package ru.radiationx.quill

import toothpick.config.Binding
import toothpick.config.Module
import javax.inject.Provider
import kotlin.reflect.KClass


fun quillModule(block: QuillModule.() -> Unit): QuillModule {
    return QuillModule().apply(block)
}

open class QuillModule {

    internal val tpModule = Module()

    fun <T : Any> instance(
        clazz: KClass<T>,
        qualifier: KClass<out Annotation>? = null,
        block: () -> T,
    ) {
        tpModule.bind(clazz.java)
            .applyQualifier(qualifier)
            .toProviderInstance { block.invoke() }
            .providesSingleton()
    }

    fun <T : Any> instance(
        clazz: KClass<T>,
        value: T,
        qualifier: KClass<out Annotation>? = null,
    ) {
        tpModule.bind(clazz.java)
            .applyQualifier(qualifier)
            .toInstance(value)
    }

    fun <T : Any> single(
        clazz: KClass<T>,
        qualifier: KClass<out Annotation>? = null,
    ) {
        tpModule.bind(clazz.java).applyQualifier(qualifier).singleton()
    }

    fun <P : Any, C : P> singleImpl(
        clazzParent: KClass<P>,
        clazzChild: KClass<C>,
        qualifier: KClass<out Annotation>? = null,
    ) {
        tpModule.bind(clazzParent.java)
            .applyQualifier(qualifier)
            .to(clazzChild.java)
            .singleton()
    }

    fun <T : Any, P : Provider<T>> singleProvider(
        clazz: KClass<T>,
        providerClazz: KClass<out Provider<T>>,
        qualifier: KClass<out Annotation>? = null,
    ) {
        tpModule.bind(clazz.java)
            .applyQualifier(qualifier)
            .toProvider(providerClazz.java)
            .providesSingleton()
    }

    inline fun <reified T : Any> single() {
        single(T::class)
    }

    inline fun <reified P : Any, reified C : P> singleImpl(
        qualifier: KClass<out Annotation>? = null,
    ) {
        singleImpl(P::class, C::class, qualifier)
    }

    inline fun <reified T : Any> instance(
        qualifier: KClass<out Annotation>? = null,
        noinline block: () -> T,
    ) {
        instance(T::class, qualifier, block)
    }

    inline fun <reified T : Any> instance(
        value: T,
        qualifier: KClass<out Annotation>? = null,
    ) {
        instance(T::class, value, qualifier)
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Any> instanceAsIs(
        value: T,
        qualifier: KClass<out Annotation>? = null,
    ) {
        val clazz = value::class as KClass<T>
        instance(clazz, value, qualifier)
    }

    inline fun <reified T : Any, reified P : Provider<T>> singleProvider(
        qualifier: KClass<out Annotation>? = null,
    ) {
        singleProvider(T::class, P::class, qualifier)
    }

    private fun <T> Binding<T>.CanBeNamed.applyQualifier(
        qualifier: KClass<out Annotation>?,
    ): Binding<T>.CanBeBound {
        return qualifier?.let { withName(qualifier.java) } ?: this
    }
}