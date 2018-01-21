package ru.radiationx.anilibria.utils.bbparser.models

import ru.radiationx.anilibria.utils.bbparser.BbSequencer

open class BbNode(val id: Int, var tag: String = NODE_ROOT) {
    companion object {
        const val NODE_ROOT = "BBROOT"
        const val NODE_TEXT = "BBTEXT"
    }

    var parent: BbNode? = null
    val attributes = mutableMapOf<String, String>()
    val childs = mutableListOf<BbNode>()
    var text: String = ""

    fun toSequence(): List<BbTypedOp> = BbSequencer().toSequence(this)

    fun print() {
        print(this)
    }

    private fun print(bbNode: BbNode, level: Int = 0) {
        val isCode = bbNode.tag != NODE_TEXT
        var tabs = ""
        for (i in 0 until level) {
            tabs += "\t"
        }
        if (isCode) {
            var attrs = ""
            if (bbNode.attributes.isNotEmpty()) {
                attrs = "{"
                bbNode.attributes.forEach {
                    attrs += "${it.key}=${it.value} "
                }
                attrs += "}"
            }
            println("$tabs[${bbNode.tag}:${bbNode.id}]$attrs")

        }
        if (bbNode.tag == NODE_TEXT) {
            println("$tabs'${bbNode.text}'")
        }
        var localLevel = level + 1
        bbNode.childs.forEach {
            print(it, localLevel)
        }
        localLevel--

        if (isCode) {
            println("$tabs[/${bbNode.tag}:${bbNode.id}]")
        }
    }
}