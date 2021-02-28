package ru.radiationx.data.migration

interface MigrationExecutor {
    fun execute(current: Int, lastSaved: Int, history: List<Int>)
}