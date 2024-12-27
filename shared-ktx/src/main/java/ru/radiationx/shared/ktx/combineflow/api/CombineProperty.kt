package ru.radiationx.combineflow.api

import kotlin.properties.ReadOnlyProperty

interface CombineProperty<T> : ReadOnlyProperty<Any?, T>