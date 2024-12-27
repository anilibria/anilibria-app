package ru.radiationx.combineflow.internal

internal class CombineCollector<R>(
    val block: suspend () -> R
)