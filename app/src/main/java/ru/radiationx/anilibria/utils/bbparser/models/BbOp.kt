package ru.radiationx.anilibria.utils.bbparser.models

class BbOp(val op: Int, val node: BbNode) {
    companion object {
        const val OPEN: Int = 2
        const val APPEND: Int = 4
        const val CLOSE: Int = 8
    }

    enum class Type { TEXT, BLOCK }
}

